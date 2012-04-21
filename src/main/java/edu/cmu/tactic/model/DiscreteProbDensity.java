package edu.cmu.tactic.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import mcr.MxFunction;

import org.slf4j.Logger;

public class DiscreteProbDensity {
	@Inject
	Logger log;
	
	double min;
	double max;
	double interval;
	double offset = 0;
	double[] pdf;
	int numSlots;
	Long rawCount;
	double[] raw = null;
	
	public DiscreteProbDensity(int numSlots, double min, double max, double offset) {
		this.numSlots = numSlots;
		this.min = min;
		this.max = max;
		this.interval = (max-min)/numSlots;
		this.offset = offset;
		pdf = new double[numSlots];
	}
	
	public DiscreteProbDensity(DiscreteProbDensity origin) {
		this.numSlots = origin.numSlots;
		this.min = origin.min;
		this.max = origin.max;
		this.interval = origin.interval;
		this.rawCount = origin.rawCount;
		this.offset = origin.offset;
		this.pdf = origin.pdf.clone();
	}
	
	public void add(double value) {
		int slot = (int)Math.round((value-min) / interval);
		// bounded slot
		if (slot >= numSlots) slot = numSlots-1;
		pdf[slot]++;
	}
	public void convert(double[] value) {
		for (int i=0;i<value.length;i++) {
			int slot = (int)Math.round((value[i]-min) / interval);
			// 	bounded slot
			if (slot >= numSlots) slot = numSlots-1;
			pdf[slot]++;
		}
		rawCount = Long.valueOf(value.length);
		raw = value;
		doNormalize();
	}
	
	/**
	 * Adding pdf (convolution) assuming this distribution and b has the same interval
	 * @param b
	 * @return
	 */
	public DiscreteProbDensity conv(DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(this.numSlots + b.numSlots, this.min+b.min, this.max+b.max,this.offset);
		for (int x=0;x<result.numSlots;x++) {
			double sum = 0;
			int stop = (x<this.numSlots)?x:this.numSlots-1;
			int start = (x<this.numSlots)?0:x-this.numSlots+1;
			for (int n = start; n<=stop; n++) {
				sum += this.pdf[x-n] * b.pdf[n];
			}
			result.pdf[x] = sum;
		}
		return result;
	}
	
	public DiscreteProbDensity deconv(DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(this.numSlots, this.min, this.max, this.offset);
		int startB = 0;
		// shift known vector (must start with nonzero element)
		while (b.pdf[startB] <= 0) startB++; 
		double divider = b.pdf[startB];
		double[] dummy = new double[result.numSlots*2];
		
		//System.out.println("divider:"+divider+" startB:"+startB);
		for (int x=0;x<result.numSlots;x++) {
			//System.out.print("x["+x+"]: ");
			int startIndex = x-1;
			double knownSum = 0;
			for (int i=startB+1;i<b.numSlots;i++) {
				//System.out.print("r"+(startIndex)+".b"+i+" ");
				if (startIndex >= 0 && startIndex < dummy.length) {
					knownSum += dummy[startIndex] * b.pdf[i];
				}
				startIndex--;
			}
			//System.out.print("sum:"+knownSum);
			dummy[x] = (this.pdf[x] - knownSum) / divider;
			if (dummy[x] < 0) dummy[x] = 0;
			//System.out.print(" dummy["+(x)+"]:"+dummy[x]);
			//System.out.println();
		}
		
		for (int x=0;x<result.numSlots;x++) {
			result.pdf[x] = dummy[x+startB];
		}
		return result;
	}
	
	/**
	 * Truncated convolution
	 * @param b
	 * @return
	 */
	public DiscreteProbDensity tconv(DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(this.numSlots, this.min, this.max,this.offset);
		result.raw = null;
		for (int x=0;x<result.numSlots;x++) {
			double sum = 0;
			int stop = (x<this.numSlots)?x:this.numSlots-1;
			int start = (x<this.numSlots)?0:x-this.numSlots+1;
			for (int n = start; n<=stop; n++) {
				sum += this.pdf[x-n] * b.pdf[n];
			}
			result.pdf[x] = sum;
		}
		
		// force left shift by the offset amount, to adjust the offset
		int convOffset = (int)Math.round(offset/interval);
		for (int x=0;x<result.numSlots - convOffset; x++) {
			result.pdf[x] = result.pdf[x+convOffset];
		}
		for (int x=result.numSlots-convOffset;x<result.numSlots;x++) {
			result.pdf[x] = 0;
		}
		return result;
	}
	
	/**
	 * Subtracting pdf (covariance) measure a cross-relation between two distributions
	 * @param b
	 * @return
	 */
	public DiscreteProbDensity cov(DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(this.numSlots + b.numSlots, this.min+b.min, this.max+b.max,this.offset);
		for (int x=0;x<result.numSlots;x++) {
			double sum = 0;
			int start = x-b.numSlots+1;
			for (int n = 0; n < this.numSlots; n++) {
				if (n < b.numSlots && n+start > 0 && n+start<this.numSlots)	
					sum += this.pdf[n+start] * b.pdf[n];
			}
			result.pdf[x] = sum;
		}
		return result;
	}
	
	/**
	 * Subtracting pdf & truncated (covariance) measure a cross-relation between two distributions
	 * @param b
	 * @return
	 */
	public DiscreteProbDensity tcov(DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(this.numSlots, this.min, this.max, this.offset);
		for (int x=0;x<result.numSlots;x++) {
			double sum = 0;
			int start = x-b.numSlots+1;
			for (int n = 0; n < this.numSlots; n++) {
				if (n < b.numSlots && n+start > 0 && n+start<this.numSlots)	
					sum += this.pdf[n+start] * b.pdf[n];
			}
			result.pdf[x] = sum;
		}
		return result;
	}
	
	/**
	 * Perform normalized pdf max algebra (convert to cdf and take min)
	 * @param b
	 * @return
	 */
	public DiscreteProbDensity normMax(DiscreteProbDensity b) {
		return (toCdf().min(b.toCdf()).toPdf()).normalize();
	}
	
	public DiscreteCumuDensity toCdf() {
		return new DiscreteCumuDensity(this);
	}
	
	public long count() {
		double freqCount = 0;
		for (int i=0;i<numSlots;i++) {
			freqCount += pdf[i];
		}
		return Math.round(freqCount);
	}
	
	public void doNormalize() {
		double freqCount = 0;
		
		for (int i=0;i<numSlots;i++) {
			freqCount += pdf[i];
		}
		if (freqCount > 0) {
			for (int i=0;i<numSlots;i++) {
				pdf[i] = pdf[i]/freqCount;
			}
		}		
	}
	
	// cut off value above 3sd
	public DiscreteProbDensity cutoff(double cutpoint) {
		double val = min+interval/2 - offset;
		//double minval = min+interval/2;
		for (int i=0;i<numSlots;i++) {
			if (val > cutpoint) {
				pdf[i] = pdf[i] * Math.exp((cutpoint-val)/interval);
			}
			val += interval;
		}
		doNormalize();
		return this;
	}
	
	public DiscreteProbDensity cutoff() {
		double mean = average();
		double sd = stdev();		
		return cutoff(mean + 5*sd);
		//return cutoff(10.0);
	}
	
	/**
	 * Return truncated normalized convolution with the given signal
	 * 
	 * @param b
	 * @return
	 */
	public DiscreteProbDensity tnormConv(DiscreteProbDensity b) {
		DiscreteProbDensity result = tconv(b);
		result.doNormalize();
		return result;
	}
	
	public DiscreteProbDensity normalize() {
		DiscreteProbDensity result = new DiscreteProbDensity(this.numSlots, this.min, this.max,this.offset);		
		double freqCount = 0;
		
		for (int i=0;i<numSlots;i++) {
			freqCount += pdf[i];
		}
		if (freqCount > 0) {
			for (int i=0;i<numSlots;i++) {
				result.pdf[i] = pdf[i]/freqCount;
			}
		}
		return result;
	}

	public double average() {
		double freqCount = 0;
		double sum = 0;
		double val = min+interval/2 - offset;
		double minval = min+interval/2;
		for (int i=0;i<numSlots;i++) {
			freqCount += pdf[i];
			if (val > 0)
				sum += pdf[i]*val;
			else
				sum += pdf[i]*minval;
			val += interval;
		}
		return sum/freqCount;
	}
	
	public double mode() {
		double maxprob = -1;
		double maxvalue = min;
		double val = min+interval/2 - offset;
		double minval = min+interval/2;
		for (int i=0;i<numSlots;i++) {
			if (pdf[i] > maxprob) {
				maxprob = pdf[i];
				maxvalue = val;
			}
			val += interval;
		}
		if (maxvalue < minval) maxvalue = minval;
		return maxvalue;
	}
	
	public double stdev() {
		double avg = average();
		
		double freqCount = 0;
		double sum = 0;
		double val = min+interval/2 - offset;
		double minval = min+interval/2;
		for (int i=0;i<numSlots;i++) {
			freqCount += pdf[i];
			if (val > 0)
				sum += pdf[i]*(val-avg)*(val-avg);
			else
				sum += pdf[i]*(minval-avg)*(minval-avg);
			val += interval;
		}
		return Math.sqrt(sum/freqCount);
	}
	
	@Override
	public String toString() {
		return "(pdf avg:"+average()+" stdev:"+stdev()+" mode:"+mode()+")";
	}
	
	/**
	 * Calculate value at the given percentile (for normalized pdf only)
	 * @param percent
	 *
	 * @return percentile rank
	 */
	public double percentile(double percent) {
		double sum = 0;
		double target = percent/100;
		
		for (int i=0;i<numSlots;i++) {
			sum += pdf[i];
			if (sum > target) {
				// do linear adjustment
				return interval*(i + (target - (sum-pdf[i])) / pdf[i]) - offset;
			}
		}
		return numSlots * interval - offset;
	}
	
	public void print() {
		for (int i=0;i<pdf.length;i++) {
			System.out.println((min+i*interval)+":"+pdf[i]);
		}
	}
	
	public DiscreteProbDensity ensurePositive() {
		double sum = 0.0;
		for (int i=0;i<pdf.length;i++) {
			if (pdf[i]<0) pdf[i]=0;
			sum+=pdf[i];
		}
		if (sum <= 0) {
			log.debug("***** PDF smoothing no sum: set peak at 0 / sum {} to 1.0",sum);
			sum = 1.0;
			pdf[0] = 0.999;
			pdf[1] = 0.001;
		}
		for (int i=0;i<pdf.length;i++) {
			pdf[i] = pdf[i]/sum;
		}
		
		return this;
	}
	
	public DiscreteProbDensity smooth() {
		int peak = 0;
		for (int i=0;i<pdf.length;i++) {
			if (pdf[i] > pdf[peak]) peak = i;
		}
		
		double sum = pdf[peak];			
		boolean zeroset = false;
		int idx = peak;
		
		while (idx<pdf.length-1) {
			idx++;
			if (zeroset || pdf[idx] < 0) {
				zeroset = true;
				pdf[idx]=Math.abs(pdf[idx]/(idx-peak));
			}
			sum+=pdf[idx];				
		}
		idx = peak;
		zeroset = false;
		while (idx>0) {
			idx--;
			if (zeroset || pdf[idx] < 0) {
				zeroset = true;
				//pdf[idx]=Math.abs(pdf[idx]/(peak-idx))
				pdf[idx]=0;
			}
			sum+=pdf[idx];
		}
		/*
		for (int i=0;i<pdf.length;i++){
			if (pdf[i] < 0) pdf[i] = 0
			sum += pdf[i]
		}
		*/

		if (sum <= 0) {
			log.debug("***** PDF smoothing no sum: set peak at {} pdf {} / sum {} to 1.0",new Object[] { peak,pdf[peak],sum });				
			sum = 1.0;
			pdf[peak] = 0.999;
			pdf[peak+1] = 0.001;
		}
		for (int i=0;i<pdf.length;i++){
			pdf[i] = pdf[i]/sum;
		}
		return this;		
	}
	
	DiscreteProbDensity shiftByValue(double vshift) {
		if (vshift > 0) return rshiftByValue(vshift);
		else return lshiftByValue(-vshift);
	}
	DiscreteProbDensity rshiftByValue(double vshift) {
		DiscreteProbDensity result = duplicate();
		int shift = (int)Math.round(vshift/interval);
		for (int i=result.pdf.length-1;i >= shift;i--){
			result.pdf[i] = result.pdf[i-shift];
		}
		for (int i=0;i<shift;i++){
			result.pdf[i] = 0;
		}
		return result;
	}
	DiscreteProbDensity lshiftByValue(double vshift) {
		DiscreteProbDensity result = duplicate();
		int shift = (int)Math.round(vshift/interval);
		//def sum = 0
		for (int i=0;i<result.pdf.length;i++) {
			if (i+shift >= result.pdf.length) result.pdf[i] = 0;
			else {
				//if (i<shift) sum+=result.pdf[i]
				result.pdf[i] = result.pdf[i+shift];					
			}
		}
		//result.pdf[0] += sum
		result.doNormalize();
		return result;
	}
	
	// Shift by index here
	DiscreteProbDensity rshift(int shift) {
		DiscreteProbDensity result = duplicate();
		for (int i=result.pdf.length-1;i >= shift;i--){
			result.pdf[i] = result.pdf[i-shift];
		}
		for (int i=0;i<shift;i++){
			result.pdf[i] = 0;
		}
		return result;
	}
	
	DiscreteProbDensity duplicate() {
		/*
		DiscreteProbDensity result = new DiscreteProbDensity(numSlots,min,max,offset)			
		for (int i=0;i<pdf.length;i++){
			result.pdf[i] = pdf[i]
		}
		 */
		DiscreteProbDensity result = new DiscreteProbDensity(this);
		return result;
	}
	
	// Given another pdf and its conditional prob, calculate the merged pdf
	DiscreteProbDensity distribute(double condProb, DiscreteProbDensity densityPair) {
		DiscreteProbDensity result = duplicate();
		double sum = 0;
		for (int i=0; i<result.pdf.length;i++){
			result.pdf[i] = ((1-condProb)*result.pdf[i]) + (condProb*densityPair.pdf[i]);
			sum += result.pdf[i];
		}
		if (sum <= 0) sum = 1;
		for (int i = 0; i<result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;
	}
	
	// Given another pdf, calculate the merged pdf, assuming uniform distribution
	DiscreteProbDensity distribute(DiscreteProbDensity densityPair) {
		DiscreteProbDensity result = duplicate();
		double sum = 0;
		for (int i=0; i<result.pdf.length;i++){
			result.pdf[i] += densityPair.pdf[i];
			sum += result.pdf[i];
		}
		if (sum <= 0) sum = 1;
		for (int i=0;i<result.pdf.length;i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;
	}
	
	// Given an input pdf and its probability, calculate the pdf of the current one subtracted by the input
	DiscreteProbDensity remainDistribute(double prob,DiscreteProbDensity densityPair) {
		DiscreteProbDensity result = duplicate();
		double sum = 0;
		for (int i=0; i<result.pdf.length;i++){
			result.pdf[i] -= densityPair.pdf[i]*prob;
			if (result.pdf[i] < 0) result.pdf[i] = 0;
			sum += result.pdf[i];
		}
		if (sum <= 0) sum = 1;
		for (int i=0; i < result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;
	}

	// Given a pdf and its conditional probability, extract the original pdf
	DiscreteProbDensity extract(double condProb, DiscreteProbDensity densityPair) {
		DiscreteProbDensity result = duplicate();
		double sum = 0;
		for (int i=0; i<result.pdf.length;i++){
			result.pdf[i] -= condProb*densityPair.pdf[i]/(1-condProb);
			// make sure the value remain positive
			if (result.pdf[i] < 0) {
				result.pdf[i] = 0;
			}
			sum += result.pdf[i];
		}
		if (sum <= 0) sum = 1;
		for (int i = 0; i < result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;
	}
	
	double[] generateRaw() {
		int sampleCount = 1000;
		List<Double> samples = new LinkedList<Double>();
		double initValue = interval/2;
		for (int i=0;i<pdf.length;i++) {
			int limit = (int)Math.round(pdf[i]*sampleCount);
			if (limit > 0) {
				double incremental = interval/limit;
				for (int k=0;k<limit;k++) {
					samples.add(initValue+k*incremental);
				}
			}
			initValue += interval;
		}
		log.debug("**Generate {} samples",samples.size());
		while (samples.size() < sampleCount) {
			samples.add(initValue);
		}
		while (samples.size() > sampleCount) {
			samples.remove(0);
		}
		
		double[] out = new double[sampleCount];
		Iterator<Double> iter = samples.iterator();
		for (int i=0;i<sampleCount;i++) {
			out[i] = iter.next(); 
		}
		return out;
	}

	DiscreteProbDensity filter(DiscreteProbDensity b) {
		DiscreteProbDensity result = duplicate();
		//def matlabResult = proxy.returningEval("imfilter(${result.pdf},${b.pdf},'same','conv')",1)
		double[] matlabResult = MxFunction.imfilter(result.pdf, b.pdf);
		result.pdf = matlabResult;
		return result;
	}

	double[] getQuantile(double[] qArray) {
		int qIndex = 0;
		double[] result = new double[qArray.length];
		double sum = 0;
		for (int i=0;i<numSlots;i++) {
			sum = sum+pdf[i];
			while (sum >= qArray[qIndex]) {
				result[qIndex] = interval*(i + (qArray[qIndex] - (sum-pdf[i])) / pdf[i]) - offset;
				qIndex++;
				if (qIndex == qArray.length) {
					return result;
				}
			}
		}
		return result;
	}
	
	public DiscreteProbDensity setRawCount(long rawCount) {
		this.rawCount = rawCount;
		return this;
	}
	
	public double[] getPdf() {
		return pdf;
	}
	
	public static void main(String[] args) {
		DiscreteProbDensity dpd = new DiscreteProbDensity(10, 0, 100, 0);
		dpd.add(10);
		dpd.add(20);
		dpd.add(20);
		dpd.add(30);
		System.out.println("---A---");
		dpd.print();

		DiscreteProbDensity b = new DiscreteProbDensity(10, 0, 100, 0);
		b.add(10);
		b.add(10);
		b.add(20);
		System.out.println("---B---");
		b.print();
				
		System.out.println("---A + B (trunc)--");
		DiscreteProbDensity dpt = dpd.tconv(b);
		dpt.print();
		
		System.out.println("--- A + B - A (Deconv) ---");
		dpt.deconv(b).print();
		
		/*
		DiscreteProbDensity dpdcb = dpd.conv(b);
		System.out.println("---A + B (notrunc)----");
		dpdcb.print();
		*/
		
		/*
		System.out.println("---A + B - B (trunc) ----");
		dpt.tcov(b).print();
		
		System.out.println("---A - A (autocorr)----");
		dpd.tcov(dpd).print();
		*/
	}
}
