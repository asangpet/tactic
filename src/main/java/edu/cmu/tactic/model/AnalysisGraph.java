package edu.cmu.tactic.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;

public class AnalysisGraph extends InstanceGraph {
	Node root = null;
	boolean shapeshift = false;
	boolean nonparamPredict = true;
	
	@Inject	MatlabUtility matlab;	
	@Inject Logger log;

	ParametricDensity getParamFit(DiscreteProbDensity pdf, boolean nofit) {
		if (nofit) return new ParametricDensity(pdf, new double[] {1,1,1}); 
		if (pdf.raw!=null) 
			return matlab.getGevParamFit(pdf,pdf.raw); 
		else 
			return matlab.getGevParamFit(pdf);
	}
	
	ParametricDensity getParamFitShape(DiscreteProbDensity pdf,double shape, boolean nofit) {
		if (nofit) return new ParametricDensity(pdf, new double[] {1,1,1});
		if (pdf.raw!=null)
			return matlab.getGevParamFit(pdf,pdf.raw,shape);
		else
			return matlab.getGevParamFit(pdf,null,shape);
	}
	
	DiscreteProbDensity getPdf(double shape,double scale,double location) { 
		return matlab.gev(shape,scale,location); 
	}
	//def getPdf = { shape, scale,location -> matlab.norm(scale,location) }
	
	double rawDistThreshold = 0.2;
	
	void analyze(Map<String,DiscreteProbDensity> densityMap) {
		resetMark();
		
		if (root == null) root = nodeList.get(0);
		for (Node it:nodeList) {
			it.analysisResponse = null;
			it.model = null;
		}
		for (Map.Entry<String, DiscreteProbDensity> item:densityMap.entrySet()) {
			if (nodes.get(item.getKey()) != null) {
				nodes.get(item.getKey()).serverResponse = item.getValue();
			}
		}
		
		// calculate average tier request count (need for multiple convolution at heavily used tier)
		for (Node node:nodeList) {		
			if (node.serverResponse != null) {
				int uptierCount = 0;
				if (node.parents != null) {

					for (Node parent:node.parents.nodes) {
						if (parent.serverResponse != null)
							uptierCount += parent.serverResponse.rawCount;
					}
					log.debug("------Estimating tier request {} vs {}",uptierCount,node.serverResponse.rawCount);
					if (uptierCount > 0) node.requestCount = Math.round(node.serverResponse.rawCount / uptierCount);
					if (node.requestCount < 1) node.requestCount = 1;
				}
			}
		}
		
		analyzeResponse(root);
		predict(new LinkedHashMap<String, DiscreteProbDensity>());
	}
	
	void predict(Map<String, DiscreteProbDensity> densityMap) {
		resetMark();
		
		if (root == null) root = nodeList.get(0);
		
		for (Node it:nodeList) {
			it.analysisResponse = null;
		}
		for (Map.Entry<String, DiscreteProbDensity> entry:densityMap.entrySet()) {
			Node nodeEntry = nodes.get(entry.getKey());
			nodeEntry.serverResponse = entry.getValue();
			nodeEntry.edited = true;
		}
		predictResponse(root);
	}
	
	void predictTransfer(Map<String, Double> transferMap) {
		resetMark();
		
		if (root == null) root = nodeList.get(0);
		for (Node it:nodeList) {
			it.analysisResponse = null;
		}
		
		for (Map.Entry<String, Double> entry: transferMap.entrySet()) {
			Node nodeEntry = nodes.get(entry.getKey());
			
			nodeEntry.shiftValue = entry.getValue();
			nodeEntry.transferEdited = true;
			if ((nodeEntry.model!=null) && (nodeEntry.model.transfer!=null) && (nodeEntry.model.transfer.nonparamPdf != null)) {
				double ranges = Math.abs(nodeEntry.model.transfer.outputPdf.average()-nodeEntry.model.transfer.inputPdf.average());
				nodeEntry.model.transfer.editedNonparamPdf = nodeEntry.model.transfer.nonparamPdf.shiftByValue(entry.getValue()*ranges); 
			}
		}
		predictResponse(root);
	}
	
	double setCutoff(DiscreteProbDensity pdf) {
		return pdf.average()+5*pdf.stdev();
	}
	
	void analyzeResponse(Node node) {
		Subgraph children = getChildren(node);
		Set<Link> links = children.links;
		Set<Node> childs = children.nodes;
		
		if (node.mark) {
			// We've already considered this node
			return;
		}
		
		if (childs.size() == 0) {
			// terminal case, should fit a distribution
			if (node.serverResponse != null) {
				if (node.model == null) {
					// 	here we should try to generate the model for response
					ParametricDensity fitResult = getParamFit(node.serverResponse,false);
				
					// use fit result as the model
					node.model = new NodeModel(fitResult.pdf, fitResult.param, node.serverResponse.rawCount);
					node.model.cutoff = setCutoff(node.serverResponse);				
					log.debug("{} - {} - {}",new Object[] {node,node.serverResponse.average(),node.model.param});

					node.analysisResponse = new ParametricDensity(fitResult);
					node.analysisResponse.pdf.rawCount = node.serverResponse.rawCount;
					node.modelpdf = fitResult.pdf;
					// TODO: replace this with fit result					
					//node.model.pdf = node.serverResponse
					//node.modelpdf = node.serverResponse
					//node.analysisResponse.pdf = node.serverResponse
					
					log.debug("{} analyzed - {}",node,node.analysisResponse);
					node.mark = true;

				} else {
					log.debug("**Node {} model have already been generated", node);
				}
			} else {
				log.debug("No-information node during model creation (should be cached node only) - {}",node);
			}
		} else {
			// first we try to analyze all the children
			for (Node it:childs) {
				analyzeResponse(it);
			}
			
			List<ParametricDensity> compResp = new ArrayList<ParametricDensity>();
			List<ParametricDensity> distResp = new LinkedList<ParametricDensity>();
			List<Double> distProb = new LinkedList<Double>();
			List<Integer> requestCounter = new ArrayList<Integer>();
			double totalRawCount = 0;
			double existingDistProb = 0.0;
			List<Link> distLinks = new LinkedList<Link>();
			List<Link> unknownLink = new LinkedList<Link>();
			// then we attempt to combine the result based on link type
			for (Link link:links) {
				if (link.target.analysisResponse != null) {
					if (link.type instanceof CompositionDependency) {
						for (int i=0;i<link.target.requestCount;i++) { 							
							compResp.add(link.target.analysisResponse);
							requestCounter.add(link.target.requestCount); 
						}
					} else if (link.type instanceof DistributionDependency) {
						distLinks.add(link);
						distResp.add(link.target.analysisResponse);
						if (link.type.distProb == null) {
							// Adjust distribution probability based on traffic here
							totalRawCount += link.target.analysisResponse.pdf.rawCount; 
							log.debug("Request count on {} (parent {}) =${}/{}", new Object[] {link.target,node,link.target.analysisResponse.pdf.rawCount,totalRawCount});
							//link.type.distProb = (double)link.target.analysisResponse.pdf.rawCount/node.serverResponse.rawCount
						} else {
							existingDistProb += link.type.distProb;
						}
						//distProb << link.type.distProb
					}
				} else {
					unknownLink.add(link);
				}
			}
			
			// rebalance distribution link probability
			if (existingDistProb < 1.0) {
				totalRawCount = totalRawCount / (1.0-existingDistProb);
				if ((node.serverResponse!=null) && (node.serverResponse.rawCount != null)) {
					// compare estimated total with the actual response on the server
					// use the maximum
					totalRawCount = Math.max(totalRawCount, node.serverResponse.rawCount);
				}
				for (Link link:distLinks) {
					if (link.type.distProb == null) {
						link.type.distProb = link.target.analysisResponse.pdf.rawCount / totalRawCount;
						log.debug("Assigned distribution prob for {} to {}",link,link.type.distProb); 
					}
					distProb.add(link.type.distProb);
				}
			} else {
				for (Link l:distLinks) {
					distProb.add(l.type.distProb);
				}
			}
			
			// first we convolve all the composite response
			DiscreteProbDensity compositeRespPdf = null;
			if (compResp.size()>0) {
				compositeRespPdf = compResp.get(0).pdf;
				for (int i=1;i<compResp.size();i++) {
					compositeRespPdf = compositeRespPdf.tconv(compResp.get(i).pdf);
					compositeRespPdf.rawCount += compResp.get(i).pdf.rawCount / requestCounter.get(i);
				}
			}
			
			// next we calculate the response from distribution link
			DiscreteProbDensity distRespPdf = null;
			if (distResp.size() > 0) {
				List<DiscreteProbDensity> dPdf = new ArrayList<DiscreteProbDensity>();
				for (ParametricDensity it:distResp) {
					dPdf.add(it.pdf);
				}
				distRespPdf = matlab.multiDistribute(dPdf,distProb);				
				distRespPdf.rawCount = (long) 0;
				for (ParametricDensity it:distResp) {
					distRespPdf.rawCount += it.pdf.rawCount;
				}
			}
			
			if (unknownLink.size() == 0) {
				// The node is ready to calculate its transfer function (no unknown link)				
				
				//////////////////////////////////////////////////////////
				// Handle model calculation here, calculate transfer function
				if (node.serverResponse != null) {
					if (node.model == null) node.model = new NodeModel();
					node.model.cutoff = setCutoff(node.serverResponse);
					node.model.rawCount = node.serverResponse.rawCount;
					node.analysisResponse = new ParametricDensity();
					node.analysisResponse.rawCount = node.serverResponse.rawCount;
					//node.model = [pdf:fitResult.pdf, param:fitResult.param, rawCount:node.serverResponse.rawCount]
					
					// calculate transfer function
					DiscreteProbDensity convPdf;
					
					if (distRespPdf == null) {
						convPdf = compositeRespPdf;
					} else if (compositeRespPdf == null) convPdf = distRespPdf;
					else {
						convPdf = distRespPdf.tconv(compositeRespPdf).normalize();
						convPdf.rawCount = (distRespPdf.rawCount > compositeRespPdf.rawCount)? distRespPdf.rawCount : compositeRespPdf.rawCount;
					}

					// This non-parametric transfer is the deconvolution of the output pdf and input pdf
					log.debug("Deconvolution {} with {}",node.serverResponse.average(),convPdf.average());
					DiscreteProbDensity modelTransfer = matlab.deconvreg(node.serverResponse,convPdf);
					//def serverFit = getParamFit(node.serverResponse,false)
					//def convFit = getParamFit(convPdf,false)									
					//def modelTransfer = matlab.deconvreg(serverFit.pdf,convFit.pdf).cutoff(1)
					//Log.info("Use model transfer ${serverFit.pdf.average()} with ${convPdf.average()}")
					
					//def newCompResp = []
					//def newDistResp = []
					//int newCount = 0;
					// find parametric transfer for the result
					if (distRespPdf == null) {
						// Composite only						
						DiscreteProbDensity reconv = compositeRespPdf.filter(modelTransfer).ensurePositive().cutoff(node.model.cutoff); //.smooth()
						ParametricDensity inputGev = getParamFit(compositeRespPdf,nonparamPredict);
						
						// obtain parametric function for output
						ParametricDensity newFit = (shapeshift)?getParamFit(reconv,nonparamPredict):getParamFitShape(reconv,inputGev.param[0],nonparamPredict);
						node.modelinput = inputGev;
						log.debug("---{}--- Composite Parameters   = {}", node, newFit);
						
						if (inputGev.param[1] <= 0) inputGev.param[1] = newFit.param[1];
						if (inputGev.param[1] <= 0) inputGev.param[1] = 1;
						
						// Recalculate proper transfer
						TransferFunction transfer;
						if (shapeshift) {
							if (inputGev.param[0] <= 0) inputGev.param[0] = newFit.param[0];
							if (inputGev.param[0] <= 0) inputGev.param[0] = 1;
							
							transfer = new TransferFunction(new double[] {newFit.param[0]/inputGev.param[0], newFit.param[1]/inputGev.param[1], newFit.param[2]-inputGev.param[2]},
											modelTransfer);
						} else {
							transfer = new TransferFunction(new double[] { newFit.param[0], newFit.param[1]/inputGev.param[1], newFit.param[2]-inputGev.param[2] },
											modelTransfer);
						}
						log.info("---${node}--- Composite Transfer = ${transfer}");
						transfer.pdf = modelTransfer;						
						
						// Recalculate result using transfer
						node.model.transfer = transfer;
						
						DiscreteProbDensity predictPdf;
						if (nonparamPredict) {
							predictPdf = compositeRespPdf.filter(node.model.transfer.nonparamPdf).ensurePositive().cutoff(node.model.cutoff);
							transfer.inputPdf = compositeRespPdf.ensurePositive().cutoff(node.model.cutoff);
							transfer.outputPdf = predictPdf;
						} else if (shapeshift) {
							predictPdf = getPdf(node.model.transfer.param[0]*inputGev.param[0],
								node.model.transfer.param[1]*inputGev.param[1],
								node.model.transfer.param[2]+inputGev.param[2]);
						} else {
							predictPdf = getPdf(node.model.transfer.param[0],
								node.model.transfer.param[1]*inputGev.param[1],
								node.model.transfer.param[2]+inputGev.param[2]);
						}
						
						node.analysisResponse = new ParametricDensity(predictPdf);
						node.modelpdf = predictPdf;
						node.modeloutput = newFit.param;
	
					} else if (compositeRespPdf == null) {
						// distribution node
						double[] averageTransfer = new double[] {0,0,0};
						double[] maxTransfer = new double[] {0,0,0};						
						List<double[]> linkTransfer = new ArrayList<double[]>();
						int distCount = 0;
						
						List<DiscreteProbDensity> distPdf = new LinkedList<DiscreteProbDensity>();
						
						for (ParametricDensity it:distResp) {
							DiscreteProbDensity reconv = it.pdf.filter(modelTransfer).ensurePositive();//.smooth()
							ParametricDensity inputGev = getParamFit(it.pdf,nonparamPredict);
							ParametricDensity newFit = (shapeshift)?getParamFit(reconv,nonparamPredict):getParamFitShape(reconv,inputGev.param[0],nonparamPredict);
							log.debug("---{}--- Distributed Component Parameters = {}",node,newFit);
							
							if (inputGev.param[1] <= 0) inputGev.param[1] = newFit.param[1];
							if (inputGev.param[1] <= 0) inputGev.param[1] = 1;
							
							TransferFunction transfer;
							if (shapeshift) {
								if (inputGev.param[0] <= 0) inputGev.param[0] = newFit.param[0];
								if (inputGev.param[0] <= 0) inputGev.param[0] = 1;				
								transfer = new TransferFunction(new double[] {newFit.param[0]/inputGev.param[0], newFit.param[1]/inputGev.param[1], newFit.param[2]-inputGev.param[2]});
							} else {
								transfer = new TransferFunction(new double[] {newFit.param[0], newFit.param[1]/inputGev.param[1], newFit.param[2]-inputGev.param[2]});
							}
							log.debug("---{}--- Distributed Component Transfer = {}",node,transfer);
							distCount++;
							
							linkTransfer.add(transfer.param);
							
							averageTransfer[0] += transfer.param[0];
							averageTransfer[1] += transfer.param[1];
							averageTransfer[2] += transfer.param[2];
							if (transfer.param[0] > maxTransfer[0]) maxTransfer[0] = transfer.param[0];
							if (transfer.param[1] > maxTransfer[1]) maxTransfer[1] = transfer.param[1];
							if (transfer.param[2] > maxTransfer[2]) maxTransfer[2] = transfer.param[2];
							
							if (nonparamPredict) {
								distPdf.add(it.pdf.filter(modelTransfer));
							} else	if (shapeshift) {
								distPdf.add(getPdf(transfer.param[0]*inputGev.param[0],
									transfer.param[1]*inputGev.param[1],
									transfer.param[2]+inputGev.param[2]));
							} else {
								distPdf.add(getPdf(transfer.param[0],
									transfer.param[1]*inputGev.param[1],
									transfer.param[2]+inputGev.param[2]));
							}
						}
						/*
						linkTransfer.each {
							it[0] = maxTransfer[0]
							it[1] = maxTransfer[1]
							it[2] = maxTransfer[2]
						}
						*/
						
						averageTransfer[0] = averageTransfer[0] / distCount;
						averageTransfer[1] = averageTransfer[1] / distCount;
						averageTransfer[2] = averageTransfer[2] / distCount;
						
						
						TransferFunction transfer = new TransferFunction(averageTransfer, linkTransfer, modelTransfer);
						
						log.info("{} Distributed Transfer Parameters = {}", node, transfer);
						
						node.model.transfer = transfer;
						
						DiscreteProbDensity newDistPdf = matlab.multiDistribute(distPdf,distProb).ensurePositive().cutoff(node.model.cutoff);
						node.analysisResponse = new ParametricDensity(newDistPdf);
						node.modelpdf = newDistPdf;
						
						transfer.inputPdf = convPdf.ensurePositive().cutoff(node.model.cutoff);						
						transfer.outputPdf = newDistPdf;
					} else {
						// not gonna handle this case yet
						log.error("Heterogeneous node detected (both composition/distribution links exist");
						assert (1==0);
					}					
				}
				
				////////////////////////////////////////////////////////////
				// Handle Prediction here (if model is available)
				if (node.analysisResponse == null) {
					if (distRespPdf == null) {
						// composite only
						// node.analysisResponse = Parametric(compositeRespPdf) * transfer
						log.debug("Calculate prediction for composition node - {}",node);
					
						if (node.model != null) {
							ParametricDensity inputGev = getParamFit(compositeRespPdf,nonparamPredict);
							DiscreteProbDensity predictPdf = null;
							if (nonparamPredict) {
								predictPdf = compositeRespPdf.filter(node.model.transfer.nonparamPdf).ensurePositive().cutoff(node.model.cutoff);
							} else if (shapeshift) {
								predictPdf = getPdf(node.model.transfer.param[0]*inputGev.param[0],
													node.model.transfer.param[1]*inputGev.param[1],
													node.model.transfer.param[2]+inputGev.param[2]);
							} else {
								predictPdf = getPdf(node.model.transfer.param[0],
									node.model.transfer.param[1]*inputGev.param[1],
									node.model.transfer.param[2]+inputGev.param[2]);
							}
							node.analysisResponse = new ParametricDensity(predictPdf);
							node.modelpdf = predictPdf;
						} else {
							log.debug("Cannot find composite model for {}, forwarding result",node);
							node.analysisResponse = new ParametricDensity(compositeRespPdf);
							node.modelpdf = compositeRespPdf;													
						}

						/* Non-parametric prediction
						if (node.model) {
							node.analysisResponse = [pdf:compositeRespPdf.filter(node.model.transfer.nonparamPdf)]
						} else {
							node.analysisResponse = [pdf:compositeRespPdf]
						}
						*/
					} else if (compositeRespPdf == null) {
						// distribution only
						log.debug("Calculate prediction for distribution node - {}",node);
						// Prediction mode, apply previously shifted parameter
						if (node.model != null) {
							DiscreteProbDensity newDistPdf = null;
							int linkIndex = 0;
							List<DiscreteProbDensity> dPdf = new LinkedList<DiscreteProbDensity>();
							for (ParametricDensity it:distResp) {
								// find input response
								ParametricDensity inputGev = getParamFit(it.pdf,nonparamPredict);
								int lIndex = linkIndex;
								linkIndex++;
								double[] newParam = new double[3];
								if (shapeshift) {
									newParam[0] = node.model.transfer.linkparam.get(lIndex)[0]*inputGev.param[0];
									newParam[1] = node.model.transfer.linkparam.get(lIndex)[1]*inputGev.param[1];
									newParam[2] = node.model.transfer.linkparam.get(lIndex)[2]+inputGev.param[2];
								} else {
									newParam[0] = node.model.transfer.linkparam.get(lIndex)[0];
									newParam[1] = node.model.transfer.linkparam.get(lIndex)[1]*inputGev.param[1];
									newParam[2] = node.model.transfer.linkparam.get(lIndex)[2]+inputGev.param[2];
								}
								
								if (nonparamPredict) {
									dPdf.add(it.pdf.filter(node.model.transfer.nonparamPdf));
								} else {
									dPdf.add(getPdf(newParam[0],
										newParam[1],
										newParam[2]));
								}							
								//it.pdf.filter(node.model.transfer.nonparamPdf)								
							}
							newDistPdf = matlab.multiDistribute(dPdf,distProb).ensurePositive().cutoff(node.model.cutoff);
							node.analysisResponse = new ParametricDensity(newDistPdf);
							node.modelpdf = newDistPdf;
						} else {
							log.debug("Cannot find distributed model for {}, forwarding result", node);
							node.analysisResponse = new ParametricDensity(distRespPdf);
							node.modelpdf = distRespPdf;
						}					
					} else {
						/////////////////
						// Not handle this case yet
						log.error("Heterogeneous link state detected, abort");
						assert (1==0);
					}
				}
				
				// assign traffic counter if explicitly available
				if (node.serverResponse != null) {
					node.analysisResponse.pdf.rawCount = node.serverResponse.rawCount;					
				}												
				
			} else {
				// finally we figure out the missing probability for the distribution link			
				// need to calculate unknown
				if (compositeRespPdf == null) {					
					// distribution links only
					// TODO: for now, subtract the existing dist from the actual response
					Node targetNode = unknownLink.get(0).target;
					double sumDistProb = 0;
					for (double d:distProb) { sumDistProb += d; };
					assert sumDistProb <= 1;

					// TODO: we over shoot here to remove extra noise, should do something else like lowpass filter
					log.info("calculate distribution probability for {} with root {} - dist {}", new Object[] {targetNode, node, distRespPdf});
					ParametricDensity outputResponse = new ParametricDensity(node.serverResponse, node.serverResponse.rawCount);
					if (node.model == null) {						 
						node.model = new NodeModel(outputResponse);
					} else {
						node.model.outputResponse = outputResponse;
					}
					DiscreteProbDensity unknownPdf = node.model.outputResponse.pdf.remainDistribute(10,distRespPdf).ensurePositive();
					ParametricDensity fitResult = getParamFit(unknownPdf,nonparamPredict);

					/*					
					def analyze = fitResult.pdf.distribute(sumDistProb,distRespPdf)
					def server90 = node.serverResponse.percentile(90)
					if (Math.abs(analyze.percentile(90) - server90) > rawDistThreshold*server90) {
						// more than 10% off do not try to fit the distribution
						targetNode.analysisResponse = [pdf:unknownPdf, rawCount : (1.0-sumDistProb) * totalRawCount]												
					} else {
						targetNode.analysisResponse = [pdf:fitResult.pdf, param:fitResult.param, rawCount : (1.0-sumDistProb) * totalRawCount]
					}
					*/
					if (nonparamPredict) {
						targetNode.analysisResponse = new ParametricDensity(unknownPdf, fitResult.param, (1.0-sumDistProb) * totalRawCount);
						targetNode.model = new NodeModel(unknownPdf, fitResult.param, (1.0-sumDistProb) * totalRawCount);
						targetNode.modelpdf = unknownPdf;
					} else {
						targetNode.analysisResponse = new ParametricDensity(fitResult.pdf, fitResult.param, (1.0-sumDistProb) * totalRawCount);
						targetNode.model = new NodeModel(unknownPdf, fitResult.param, (1.0-sumDistProb) * totalRawCount);
						targetNode.modelpdf = fitResult.pdf;
					}
					
					unknownLink.get(0).type.distProb = 1.0-sumDistProb;					
					// recalculate current node analysis result
					log.debug("unknown link found - recalculate for {}",node);
					
					node.analysisResponse = null;
					node.mark = false;
					analyzeResponse(node);
				} else {
					log.error("analysis not available");
					assert (1==0);
					// need to handle other cases
				}			
			}
											
			log.debug("{} analyzed - {}",node,node.analysisResponse);
			node.mark = true;
		}
	}

	void predictResponse(Node node) {
		Subgraph children = getChildren(node);
		Set<Link> links = children.links;
		Set<Node> childs = children.nodes;
		
		if (node.mark) {
			// We've already considered this node
			return;
		}
		
		if (childs.size() == 0 || node.edited) {
			// terminal case, should fit a distribution
			if (node.edited) {
				// Refit the model to the specified response
					
				// Match pdf counter
				log.debug("{} - {} - {}",new Object[] {node,node.serverResponse,node.serverResponse.average()});
				node.analysisResponse = new ParametricDensity(node.serverResponse);
				node.analysisResponse.pdf.rawCount = node.serverResponse.rawCount;
			} else if (node.transferEdited) {				
				node.analysisResponse = new ParametricDensity(node.model.pdf.shiftByValue(node.shiftValue*node.model.pdf.average()));
			} else {
				node.analysisResponse = new ParametricDensity(node.model);
			}
			log.info("{} analyzed - {}",node,node.analysisResponse);
			node.mark = true;

		} else {
			// first we try to analyze all the children
			for (Node it:childs) {
				predictResponse(it);
			}
			
			List<ParametricDensity> compResp = new ArrayList<ParametricDensity>();
			List<ParametricDensity> distResp = new LinkedList<ParametricDensity>();
			List<Double> distProb = new LinkedList<Double>();
			List<Integer> requestCounter = new ArrayList<Integer>();
			List<Link> distLinks = new LinkedList<Link>();
			
			// then we attempt to combine the result based on link type
			for (Link link:links) {
				if (link.target.analysisResponse != null) {
					if (link.type instanceof CompositionDependency) {
						for (int i=0;i<link.target.requestCount;i++) {
							compResp.add(link.target.analysisResponse);
							requestCounter.add(link.target.requestCount); 
						}
					} else if (link.type instanceof DistributionDependency) {
						distLinks.add(link);
						distResp.add(link.target.analysisResponse);
					}
				}
			}
			
			// rebalance distribution link probability
			for (Link it:distLinks) {
				distProb.add(it.type.distProb);
			}
			
			// first we convolve all the composite response
			DiscreteProbDensity compositeRespPdf = null;
			if (compResp.size()>0) {
				compositeRespPdf = compResp.get(0).pdf;
				for (int i=1;i<compResp.size();i++) {
					compositeRespPdf = compositeRespPdf.tconv(compResp.get(i).pdf);
					compositeRespPdf.rawCount += compResp.get(i).pdf.rawCount / requestCounter.get(i);
				}
			}
			
			// next we calculate the response from distribution link
			DiscreteProbDensity distRespPdf = null;
			if (distResp.size() > 0) {
				List<DiscreteProbDensity> dPdf = new LinkedList<DiscreteProbDensity>();
				for (ParametricDensity it:distResp) {
					dPdf.add(it.pdf);
				}
				distRespPdf = matlab.multiDistribute(dPdf,distProb);
				distRespPdf.rawCount = 0L;
				for (ParametricDensity it:distResp) {
					distRespPdf.rawCount += it.pdf.rawCount;
				}
			}
			
			////////////////////////////////////////////////////////////
			// Handle Prediction here (if model is available)
			if (distRespPdf == null) {
				// composite only
				// node.analysisResponse = Parametric(compositeRespPdf) * transfer
				log.debug("Calculate prediction for composition node - {}",node);
					
				if (node.model != null) {
					ParametricDensity inputGev = getParamFit(compositeRespPdf,nonparamPredict);
					double[] newParam;
					if (shapeshift) {
						newParam = new double[] {node.model.transfer.param[0]*inputGev.param[0],
											node.model.transfer.param[1]*inputGev.param[1],
											node.model.transfer.param[2]+inputGev.param[2]};
					} else {
						newParam = new double[] {node.model.transfer.param[0],
							node.model.transfer.param[1]*inputGev.param[1],
							node.model.transfer.param[2]+inputGev.param[2]};
					}
					
					DiscreteProbDensity predictPdf = getPdf(newParam[0],newParam[1],newParam[2]);
					if (nonparamPredict) {
						if (node.transferEdited) {
							predictPdf = compositeRespPdf.filter(node.model.transfer.editedNonparamPdf).ensurePositive().cutoff(node.model.cutoff);
						} else {
							predictPdf = compositeRespPdf.filter(node.model.transfer.nonparamPdf).ensurePositive().cutoff(node.model.cutoff);
						}
					}
					node.analysisResponse = new ParametricDensity(predictPdf, newParam, inputGev.param);
				} else {
					log.debug("Cannot find composite model for {}, forwarding result",node);
					node.analysisResponse = new ParametricDensity(compositeRespPdf);
				}
			} else if (compositeRespPdf == null) {
				// distribution only
				log.debug("Calculate prediction for distribution node - {} - model {}",node,node.model);
				// Prediction mode, apply previously shifted parameter
				if (node.model!=null) {
					DiscreteProbDensity newDistPdf = null;
					int linkIndex = 0;
					List<DiscreteProbDensity> dPdf = new LinkedList<DiscreteProbDensity>();
					
					for (ParametricDensity it:distResp) {
						// find input response
						ParametricDensity inputGev = getParamFit(it.pdf,nonparamPredict);
						int lIndex = linkIndex;
						linkIndex++;
						double[] newParam = new double[3];
						if (shapeshift) {
							newParam[0] = node.model.transfer.linkparam.get(lIndex)[0]*inputGev.param[0];
							newParam[1] = node.model.transfer.linkparam.get(lIndex)[1]*inputGev.param[1];
							newParam[2] = node.model.transfer.linkparam.get(lIndex)[2]+inputGev.param[2];
						} else {
							newParam[0] = node.model.transfer.linkparam.get(lIndex)[0];
							newParam[1] = node.model.transfer.linkparam.get(lIndex)[1]*inputGev.param[1];
							newParam[2] = node.model.transfer.linkparam.get(lIndex)[2]+inputGev.param[2];
						}
						
						if (nonparamPredict) {
							if (node.transferEdited) {								
								dPdf.add(it.pdf.filter(node.model.transfer.editedNonparamPdf));
							} else {
								dPdf.add(it.pdf.filter(node.model.transfer.nonparamPdf));
							}
						} else {
							dPdf.add(getPdf(newParam[0],	newParam[1], newParam[2]));
						}
					}
					newDistPdf = matlab.multiDistribute(dPdf,distProb).ensurePositive().cutoff(node.model.cutoff);
					node.analysisResponse = new ParametricDensity(newDistPdf);
				} else {
					log.debug("Cannot find distributed model for {}, forwarding result",node);
					node.analysisResponse = new ParametricDensity(distRespPdf);
				}
			} else {
				/////////////////
				// Not handle this case yet
				log.error("Heterogeneous link state detected, abort");
				assert (1==0);
			}
				
			log.debug("{} predicted - {}",node,node.analysisResponse);
			node.mark = true;
		}
	}

		
}