package edu.cmu.tactic.model;

public class DiscreteCumuDensity extends DiscreteProbDensity {
	public DiscreteCumuDensity(int numSlots, double min, double max, double offset) {
		super(numSlots,min,max,offset);
	}
	
	public DiscreteCumuDensity(DiscreteProbDensity pdf) {
		super(pdf.numSlots,pdf.min,pdf.max,pdf.offset);
		double sum = 0;
		for (int i=0;i<numSlots;i++) {
			sum = sum+pdf.pdf[i];
			this.pdf[i] = sum;
		}
	}
	
	public double[] getData() {
		return pdf;
	}
	
	/**
	 * Calculate max CDF (works only for same range cdf)
	 * @param b
	 * @return
	 */
	public DiscreteCumuDensity max(DiscreteCumuDensity b) {
		DiscreteCumuDensity result = new DiscreteCumuDensity(Math.max(this.numSlots,b.numSlots), Math.min(this.min,b.min), Math.max(this.max,b.max), this.offset);
		for (int x=0;x<result.numSlots;x++) {
			result.pdf[x] = Math.max(this.pdf[x],b.pdf[x]);	
		}
		return result;
	}		
	
	/**
	 * Calculate min CDF (works only for same range cdf)
	 * @param b
	 * @return
	 */
	public DiscreteCumuDensity min(DiscreteCumuDensity b) {
		DiscreteCumuDensity result = new DiscreteCumuDensity(Math.max(this.numSlots,b.numSlots), Math.min(this.min,b.min), Math.max(this.max,b.max), this.offset);
		for (int x=0;x<result.numSlots;x++) {
			result.pdf[x] = Math.min(this.pdf[x],b.pdf[x]);	
		}
		return result;
	}
	
	public static DiscreteCumuDensity fromPdf(DiscreteProbDensity pdf) {
		DiscreteCumuDensity result = new DiscreteCumuDensity(pdf);
		return result;
	}
	
	public DiscreteProbDensity toPdf() {
		DiscreteProbDensity result = new DiscreteProbDensity(numSlots,min,max,offset);
		double last=0;
		for (int i=0;i<numSlots;i++) {
			result.pdf[i] = pdf[i] - last;
			last = pdf[i];
		}
		return result;
	}

	public static void main(String[] args) {
		DiscreteProbDensity dpd = new DiscreteProbDensity(10, 0, 100,0);
		dpd.add(20);
		dpd.add(22);
		dpd.add(12);
		dpd.add(13);
		dpd.add(15);
		dpd.print();
		System.out.println("-------");
		
		DiscreteCumuDensity cdf = new DiscreteCumuDensity(dpd);
		cdf.print();
		System.out.println("-------");
		

		DiscreteProbDensity b = new DiscreteProbDensity(10, 0, 100,0);
		b.add(10);
		b.add(10);
		b.add(10);
		b.add(0);
		b.print();
		System.out.println("-------");
		b.toCdf().print();
		System.out.println("-------");
		DiscreteCumuDensity maxcdf = cdf.max(b.toCdf());
		maxcdf.print();
		System.out.println("-------");
		maxcdf.toPdf().print();
	}

}
