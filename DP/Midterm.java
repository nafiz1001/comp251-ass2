import java.util.*;
import java.lang.*;
import java.io.*;

public class Midterm {
	private static int[][] dp_table;
	private static int[] penalization;
	

	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int chairs;
		try {
			chairs = Integer.valueOf(reader.readLine());
			penalization = new int[chairs];
			for (int i=0; i< chairs; i++) {
				penalization[i] = Integer.valueOf(reader.readLine());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int answer = lost_marks(penalization);
		System.out.println(answer);
	}

	public static int lost_marks_recursive(int[] penalization, int[][] dp_table, int currIndex, int precJumpReq) {
		final int NO_SOLUTION = Integer.MAX_VALUE;
		final int NOT_INITIALIZED = -1;

		final int cidx = currIndex;
		final int pjr = precJumpReq;

		if (cidx == 0 && pjr == 0) {
			return 0;
		} else if (cidx > 0 && pjr == 0) {
			return NO_SOLUTION;
		}

		// try {
		if (dp_table[cidx][pjr] != NOT_INITIALIZED) {
			return dp_table[cidx][pjr];
		}	
		// } catch (Exception e) {
		// 	System.out.println(cidx);
		// 	System.out.println(pjr);
		// 	System.exit(0);
		// }

		int minMarksLost = NO_SOLUTION;

		final int indexLeft = cidx - pjr;
		if (indexLeft >= 0) {
			minMarksLost = lost_marks_recursive(penalization, dp_table, indexLeft, pjr - 1);
		}

		final int indexRight = cidx + pjr;
		if (indexRight < penalization.length - 1) {
			minMarksLost = Math.min(minMarksLost, lost_marks_recursive(penalization, dp_table, indexRight, pjr));
		}

		if (minMarksLost == NO_SOLUTION) {
			dp_table[cidx][pjr] = minMarksLost;
		} else {
			dp_table[cidx][pjr] = minMarksLost + penalization[cidx];
		}

		return dp_table[cidx][pjr];
	}
	
	public static int lost_marks(int[] penalization) {
		final int NOT_INITIALIZED = -1;
		final int NO_SOLUTION = Integer.MAX_VALUE;

		dp_table = new int[penalization.length][penalization.length];
		for (int i = 0; i < dp_table.length; ++i) {
			for (int j = 0; j < dp_table[i].length; ++j) {
				dp_table[i][j] = NOT_INITIALIZED;
			}
		}

		int minMarksLost = NO_SOLUTION;
		final int indexLast = penalization.length - 1;
		for (int i = 0; i < indexLast; ++i) {
		        final int precJumpReq = (indexLast - i) - 1;
			// if (precJumpReq <= 0) {
			// 	System.exit(2);
			// }
			minMarksLost = Math.min(minMarksLost, lost_marks_recursive(penalization, dp_table, i, precJumpReq));
		}

		return minMarksLost + penalization[indexLast];
	}

}
