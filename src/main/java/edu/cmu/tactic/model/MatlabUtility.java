package edu.cmu.tactic.model;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import mcr.*;

public class MatlabUtility {
	@Inject
	Logger log;
	
	int maxTime = ModelConfig.maxTime;
	double offset = ModelConfig.offset;
	int rangeinterval = ModelConfig.rangeinterval;
	double[] rangeArray;
	
	public MatlabUtility() {
		MxFunction.mxInit();
		
		// generate rangeArray
		rangeArray = new double[maxTime/rangeinterval];
		double start = rangeinterval/2;
		for (int i=0;i<rangeArray.length;i++) {
			rangeArray[i] = start;
			start = start+rangeinterval;
		}		
	}

	/*
	def filterfft(a,b) {
		def result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset)
		def matlabResult = proxy.returningEval("ifft(fft(${a.pdf}).*fft(${b.pdf}))",1)
		result.pdf = matlabResult
		return result
	}

	def deconvfft(a,b) {
		def result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset)
		def matlabResult = proxy.returningEval("ifft(fft(${a.pdf})./fft(${b.pdf}))",1)
		result.pdf = matlabResult
		return result
	}
	*/
	
	DiscreteProbDensity filter(DiscreteProbDensity a,DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] matlabResult = MxFunction.imfilter(a.pdf, b.pdf);
		result.pdf = matlabResult;
		result.raw = null;
		return result;
	}
	
	DiscreteProbDensity deconvreg(DiscreteProbDensity a,DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] matlabResult = MxFunction.deconvreg(a.pdf, b.pdf);
		result.pdf = matlabResult;
		result.raw = null;
		return result;
	}
	
	int rawfitcount = 0;
	int totalfitcount = 0;
	ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw, Double shape) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] myfit = null;
		if (raw != null) {
			myfit = MxFunction.gevfit(raw);
			int start = rangeinterval/2;
			double[] matlabResult = MxFunction.gevpdf(start, maxTime-rangeinterval+start, rangeinterval, myfit[0], myfit[1], myfit[2]);
			result.pdf = matlabResult;
			rawfitcount++;
		} else {
			//myfit = MxFunction.gevfit(a.generateRaw())		
			if (shape == null) {
				myfit = MxFunction.gevfitpdf(maxTime/rangeinterval,rangeArray,a.pdf);
			} else {	
				myfit = MxFunction.gevfitpdf(maxTime/rangeinterval,rangeArray,a.pdf,shape);
				myfit = MxFunction.gevfitpdf(maxTime/rangeinterval,rangeArray,a.pdf);
			}
			double[] matlabResult = MxFunction.gevpdf(rangeArray, myfit[0], myfit[1], myfit[2]);		
			result.pdf = matlabResult;
		}
		totalfitcount++;
		log.info("***** Fitting raw: {} / {} : total", rawfitcount, totalfitcount);
		return new ParametricDensity(result.normalize(), myfit);
	}
	ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw) {
		return getGevParamFit(a, raw, null);
	}
	ParametricDensity getGevParamFit(DiscreteProbDensity a) {
		return getGevParamFit(a, null, null);
	}
	
	ParametricDensity getGpParamFit(DiscreteProbDensity a) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] rawData = a.generateRaw();
		double[] positiveData = new double[rawData.length];
		int count = 0;
		for (int i=0;i<rawData.length;i++) { 
			if (rawData[i]>0) {
				positiveData[count] = rawData[i];
				count++;
			}
		}
		
		double[] myfit = MxFunction.gpfit(positiveData);
		int start = rangeinterval/2;
		// assume 0 as the exceedance threshold
		double[] matlabResult = MxFunction.gppdf(start, maxTime-rangeinterval+start, rangeinterval, myfit[0], myfit[1], 0);
		result.pdf = matlabResult;
		return new ParametricDensity(result.normalize(), new double[] {myfit[0],myfit[1],(double)0});
	}
	
	ParametricDensity getNormParamFit(DiscreteProbDensity a) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] myfit = MxFunction.normfit(a.generateRaw());
		int start = rangeinterval/2;
		double[] matlabResult = MxFunction.normpdf(start, maxTime-rangeinterval+start, rangeinterval, myfit[0], myfit[1]);
		result.pdf = matlabResult;
		return new ParametricDensity(result.normalize(), new double[] {myfit[0],myfit[1],(double)0} );
	}

	DiscreteProbDensity movingAverage(DiscreteProbDensity a, double span) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] matlabResult = MxFunction.smooth(a.pdf, span);
		result.pdf = matlabResult;
		return result.normalize();
	}
	
	DiscreteProbDensity gev(double k,double scale,double location) {
		DiscreteProbDensity result = new DiscreteProbDensity(maxTime/rangeinterval,0,maxTime,offset);
		int start = rangeinterval / 2;
		double[] matlabResult = MxFunction.gevpdf(start,maxTime-rangeinterval+start,rangeinterval,k,scale,location);
		result.pdf = matlabResult;
		return result.normalize();
	}
	DiscreteProbDensity gp(double k,double scale,double location) {
		DiscreteProbDensity result = new DiscreteProbDensity(maxTime / rangeinterval,0,maxTime,offset);
		int start = rangeinterval/2;
		double[] matlabResult = MxFunction.gppdf(start,maxTime-rangeinterval+start,rangeinterval,k,scale,location);
		result.pdf = matlabResult;
		return result.normalize();
	}
	DiscreteProbDensity norm(double mean,double std,double extra) {
		DiscreteProbDensity result = new DiscreteProbDensity(maxTime/rangeinterval,0,maxTime,offset);
		int start = rangeinterval/2;
		double[] matlabResult = MxFunction.normpdf(start,maxTime-rangeinterval+start,rangeinterval,mean,std);
		result.pdf = matlabResult;
		return result.normalize();
	}

	DiscreteProbDensity gaussian(double mean,double sd) {
		DiscreteProbDensity result = new DiscreteProbDensity(maxTime / rangeinterval,0,maxTime,offset);
		double[] matlabResult = MxFunction.normpdf(0, maxTime-rangeinterval, rangeinterval, mean, sd);
		result.pdf = matlabResult;
		return result;
	}
	
	DiscreteProbDensity multiDistribute(List<DiscreteProbDensity> pdfVector,List<Double> probVector) {
		DiscreteProbDensity initPdf = pdfVector.get(0);
		DiscreteProbDensity result = new DiscreteProbDensity(initPdf.numSlots,initPdf.min,initPdf.max,offset);
		result.raw = null;
		double sum = 0;
		for (int i=0; i<result.pdf.length;i++){
			for (int k=0;k<pdfVector.size();k++) {
				result.pdf[i] += probVector.get(k)*pdfVector.get(k).pdf[i];
			}
			sum += result.pdf[i];
		}
		if (sum <= 0) sum = 1;
		for (int i = 0; i < result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;
	}
}