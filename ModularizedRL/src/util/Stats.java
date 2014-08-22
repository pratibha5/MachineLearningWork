package util;

import java.util.Random;

import util.MathUtil;


public class Stats {

	/**
	 * @param conf
	 *            a confusion matrix
	 * @return the relative classifier information
	 */
	public static double getRCI(double[][] conf) {
		double Hd = 0;
		double total = MathUtil.sumMatrix(conf);
		for (int i = 0; i < conf.length; i++) {
			double qi = 0;
			for (int j = 0; j < conf[i].length; j++)
				qi += conf[i][j];
			if (qi > 0) {
				Hd += -1 * qi / total * Math.log(qi / total) / Math.log(2);
			}
		}
		double Hc = 0;
		double Hoj = 0;
		double Ho = 0;
		for (int j = 0; j < conf[0].length; j++) {
			Hoj = 0.0;
			double vs = 0;
			for (int k = 0; k < conf.length; k++)
				vs += conf[k][j];
			for (int i = 0; i < conf.length; i++)
				if (conf[i][j] != 0) {
					Hoj += -1 * (conf[i][j] / vs) * Math.log(conf[i][j] / vs)
							/ Math.log(2);
				}
			Ho += vs / total * Hoj;
		}
		Hc = Hd - Ho;
		return Hc / Hd * 100;
	}

	/**
	 * @param conf
	 *            a confusion matrix
	 * @return the relative classifier information
	 */
	public static double getRCI_NewType(double[][] conf) {
		double Hd = 0;
		double total = MathUtil.sumMatrix(conf);
		boolean max = false;
		for (int i = 0; i < conf.length; i++) {
			double qi = 0;
			for (int j = 0; j < conf[i].length; j++)
				qi += conf[i][j];
			if (qi > 0) {
				if (!max && qi >= total / conf.length) {
					Hd += qi / total * Math.log(qi / total) / Math.log(2);
					max = true;
				} else
					Hd += -1 * qi / total * Math.log(qi / total) / Math.log(2);
			}
		}
		double Hc = 0;
		double Hoj = 0;
		double Ho = 0;
		for (int j = 0; j < conf[0].length; j++) {
			Hoj = 0.0;
			double vs = 0;
			for (int k = 0; k < conf.length; k++)
				vs += conf[k][j];
			for (int i = 0; i < conf.length; i++)
				if (conf[i][j] != 0) {
					if (i == j)
						Hoj += (conf[i][j] / vs) * Math.log(conf[i][j] / vs)
								/ Math.log(2);
					else
						Hoj += -1 * (conf[i][j] / vs)
								* Math.log(conf[i][j] / vs) / Math.log(2);
				}
			Ho += vs / total * Hoj;
		}
		Hc = Hd - Ho;
		return Hc / Hd * 100;
	}

	static void permuteCls(double[] cls, double[][] dmPreds,
			double[][][] conf) {
		for (int k = 0; k < conf.length; k++)
			conf[k] = Arrays.init(conf[k], 0);
		Random ran = new Random();
		for (int i = cls.length - 1; i >= 1; i--) {
			int nm = ran.nextInt(i);
			double tmp = cls[i];
			cls[i] = cls[nm];
			cls[nm] = tmp;
			for (int j = 0; j < conf.length; j++) {
				conf[j][(int) cls[i]][(int) dmPreds[j + 1][i]] += 1;
			}
		}
	}

	public static double[][] getRCI(double[][] dmPreds, int numAttrKeys) {
		double[][] rcis = Arrays.init(
				new double[10001][dmPreds.length - 1], 0);
		double[][][] conf = new double[dmPreds.length - 1][numAttrKeys][numAttrKeys];
		for (int con = 0; con < conf.length; con++)
			conf[con] = Arrays.init(
					new double[numAttrKeys][numAttrKeys], 0);
		for (int i = 1; i < dmPreds.length; i++) {
			for (int j = 0; j < dmPreds[i].length; j++) {
				conf[i - 1][(int) dmPreds[0][j]][(int) dmPreds[i][j]] += 1;
			}
		}
		double[] oldCls = dmPreds[0];
		double[] cls = new double[dmPreds[0].length];
		System.arraycopy(dmPreds[0], 0, cls, 0, dmPreds[0].length);
		int[] numG = Arrays.init(new int[rcis[0].length], 0);
		for (int k = 0; k < conf.length; k++) {
			rcis[0][k] = getRCI(conf[k]);
			System.out.print("Method " + (k + 1) + ": " + rcis[0][k]);
		}
		System.out.println("RCI difference: " + (rcis[0][0] - rcis[0][1]));
		int nm = 0;
		for (int p = 0; p < 10000; p++) {
			permuteCls(cls, dmPreds, conf);
			for (int k = 0; k < conf.length; k++) {
				rcis[p + 1][k] = getRCI(conf[k]);
				if (rcis[p + 1][k] > rcis[0][k])
					numG[k] += 1;
			}
			if (rcis[0][0] - rcis[0][1] <= rcis[p][0] - rcis[p][1])
				nm++;
		}
		double[][] pvals = Arrays.init(
				new double[2][rcis[0].length + 1], 0);
		for (int i = 0; i < rcis[0].length; i++) {
			pvals[0][i] = rcis[0][i];
			pvals[1][i] = numG[i] / ((double) 10000.0);
		}
		pvals[0][pvals[0].length - 1] = rcis[0][0] - rcis[0][1];
		pvals[1][pvals[0].length - 1] = nm / ((double) 10000.0);
		return pvals;
	}

	public static double getRCIValue(double[][] preds, int numAttrKeys) {
		double[][] conf = Arrays.init(
				new double[numAttrKeys][numAttrKeys], 0);
		for (int i = 0; i < preds[0].length; i++)
			conf[(int) preds[0][i]][(int) preds[1][i]] += 1;
		return getRCI(conf);
	}

	public static void main(String[] args) {
		double[][] t = {{18, 1, 1}, {2, 17, 1}, {1, 1, 18}};
		double val = Stats.getRCI(t);
		System.out.println("RCI of mat = " + val);
		val = Stats.getRCI_NewType(t);
		System.out.println("RCI of mat = " + val);
		double[][] s = {{20, 0, 0}, {2, 15, 3}, {2, 0, 18}};
		val = Stats.getRCI(s);
		System.out.println("RCI of mat = " + val);
		val = Stats.getRCI_NewType(s);
		System.out.println("RCI of mat = " + val);
	}
}