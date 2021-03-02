import java.util.*;
import java.lang.*;
import java.io.*;


public class Game {
	
	Board sudoku;
	
	public class Cell{
		private int row = 0;
		private int column = 0;
		
		public Cell(int row, int column) {
			this.row = row;
			this.column = column;
		}
		public int getRow() {
			return row;
		}
		public int getColumn() {
			return column;
		}
	}
	
	public class Region{
		private Cell[] matrix;
		private int num_cells;
		public Region(int num_cells) {
			this.matrix = new Cell[num_cells];
			this.num_cells = num_cells;
		}
		public Cell[] getCells() {
			return matrix;
		}
		public void setCell(int pos, Cell element){
			matrix[pos] = element;
		}
		
	}
	
	public class Board{
		private int[][] board_values;
		private Region[] board_regions;
		private int num_rows;
		private int num_columns;
		private int num_regions;
		
		public Board(int num_rows,int num_columns, int num_regions){
			this.board_values = new int[num_rows][num_columns];
			this.board_regions = new Region[num_regions];
			this.num_rows = num_rows;
			this.num_columns = num_columns;
			this.num_regions = num_regions;
		}
		
		public int[][] getValues(){
			return board_values;
		}
		public int getValue(int row, int column) {
			return board_values[row][column];
		}
		public Region getRegion(int index) {
			return board_regions[index];
		}
		public Region[] getRegions(){
			return board_regions;
		}
		public void setValue(int row, int column, int value){
			board_values[row][column] = value;
		}
		public void setRegion(int index, Region initial_region) {
			board_regions[index] = initial_region;
		}	
		public void setValues(int[][] values) {
			board_values = values;
		}

	}

	public boolean isValueValid(int[][] values, Region region, Cell cell, Cell[][] coordToCell, HashMap<Cell, Region> cellToRegion, int value) {

		// cannot contain value already present in the region
		for (final Cell otherCell : region.getCells()) {
			final int otherCellValue = values[otherCell.getRow()][otherCell.getColumn()];
			if (cell != otherCell && otherCellValue == value) return false;
		}

		final int[][] deltaIndices = {
			{0, -1}, // top
			{1, -1}, // top right
			{1, 0}, // right
			{1, 1}, // bottom right
			{0, 1}, // bottom
			{-1, 1}, // bottom left
			{-1, 0}, // left
			{-1, -1} // top left
		};

		// do not use value of neighboring cells
		for (final int[] currDeltaIndices : deltaIndices) {
			final int otherRow = currDeltaIndices[1] + cell.getRow();
			final int otherColumn = currDeltaIndices[0] + cell.getColumn();

			if (
				otherRow >= 0 &&
				otherRow < sudoku.num_rows &&
				otherColumn >= 0 &&
				otherColumn < sudoku.num_columns
			) {
				final int otherCellValue = values[otherRow][otherColumn];
				if (otherCellValue == value) return false;
			}
		}
		return true;
	}

	public String valuesToString(int[][] values, Cell[][] coordToCell, HashMap<Cell, Region> cellToRegion, Cell currentCell) {
		final StringBuffer stringBuffer = new StringBuffer();
		final String ANSI_EFFECT = "\u001B[30m\u001B[47m";
		final String ANSI_RESET = "\u001B[0m";

		for (int i=0; i<values.length;i++) {
			for (int j=0; j<values[0].length; j++) {
				if (currentCell != null && coordToCell[i][j] == currentCell) {
					stringBuffer.append(ANSI_EFFECT);
				}
				stringBuffer.append(String.format("%2d", values[i][j]));
				if (currentCell != null && coordToCell[i][j] == currentCell) {
					stringBuffer.append(ANSI_RESET);
				}
				if (j<values[0].length -1) {
					stringBuffer.append(" ");
				}
			}

			stringBuffer.append("    ");

			for (int j=0; j<values[0].length; j++) {
				for (int index = 0; index < sudoku.getRegions().length; ++index) {
					if (sudoku.getRegion(index).equals(cellToRegion.get(coordToCell[i][j]))) {
						if (currentCell != null && coordToCell[i][j] == currentCell) {
							stringBuffer.append(ANSI_EFFECT);
						}
						stringBuffer.append((char)('A' + index));
						if (currentCell != null && coordToCell[i][j] == currentCell) {
							stringBuffer.append(ANSI_RESET);
						}
						break;
					}
				}
				if (j<values[0].length -1) {
					stringBuffer.append(" ");
				}
			}

			stringBuffer.append("\n");
		}

		return stringBuffer.toString();
	}

	public int[][] copyValues(int[][] src) {
		final int[][] dst = new int[sudoku.num_rows][sudoku.num_columns];
		for (int row = 0; row < src.length; ++row) {
			for (int col = 0; col < src[row].length; ++col) {
				dst[row][col] = src[row][col];
			}
		}

		return dst;
	}

	public int[][] solver_recurse(int[][] values, Cell[][] coordToCell, HashMap<Cell, Region> cellToRegion, int rowStart, int colStart) {

		final int row = colStart >= sudoku.num_columns ? rowStart + 1 : rowStart;
		final int col = colStart >= sudoku.num_columns ? 0 : colStart;

		if (row >= sudoku.num_rows) return values;

					final Cell c = coordToCell[row][col];
					final Region r = cellToRegion.get(c);
		final int[][] valuesCopy = copyValues(values);

		if (values[c.row][c.column] == -1) {
					for (int value = 1; value <= r.getCells().length; ++value) {
						if (isValueValid(values, r, c, coordToCell, cellToRegion, value)) {
							valuesCopy[c.row][c.column] = value;

							System.out.println(valuesToString(valuesCopy, coordToCell, cellToRegion, c));

					final int[][] solution = solver_recurse(valuesCopy, coordToCell, cellToRegion, row, col + 1);

							if (solution != null) {
								return solution;
							}
						}
					}
		} else {
			return solver_recurse(valuesCopy, coordToCell, cellToRegion, row, col + 1);
				}

		return null;
	}
	
	public int[][] solver() {
		final Cell[][] coordToCell = new Cell[sudoku.num_rows][sudoku.num_columns];
		final HashMap<Cell, Region> cellToRegion = new HashMap<>();
		for (final Region r : sudoku.getRegions()) {
			// initialise indexToCell and cellToRegion
			for (final Cell c : r.getCells()) {
				coordToCell[c.getRow()][c.getColumn()] = c;
				cellToRegion.put(c, r);
			}
		}

		final int row = 0;
		final int col = 0;

				final Cell c = coordToCell[row][col];
				final Region r = cellToRegion.get(c);
		final int[][] valuesCopy = copyValues(sudoku.getValues());

		if (sudoku.getValue(c.row, c.column) == -1) {
				for (int value = 1; value <= r.getCells().length; ++value) {
					if (isValueValid(sudoku.getValues(), r, c, coordToCell, cellToRegion, value)) {
						valuesCopy[c.row][c.column] = value;

						System.out.println(valuesToString(valuesCopy, coordToCell, cellToRegion, c));

					final int[][] solution = solver_recurse(valuesCopy, coordToCell, cellToRegion, row, col + 1);

						if (solution != null) {
						return solution;
						}
					}
				}
		} else {
			sudoku.setValues(solver_recurse(valuesCopy, coordToCell, cellToRegion, row, col + 1));
		}

		return sudoku.getValues();
	}

	
	public static void main(String[] args) {
		try {
			Scanner sc = new Scanner(new File("/home/nafiz/Documents/comp251-ass2/CompleteSearch/test1.in"));
		int rows = sc.nextInt();
		int columns = sc.nextInt();
		int[][] board = new int[rows][columns];
		//Reading the board
		for (int i=0; i<rows; i++){
			for (int j=0; j<columns; j++){
				String value = sc.next();
				if (value.equals("-")) {
					board[i][j] = -1;
				}else {
					try {
						board[i][j] = Integer.valueOf(value);
					}catch(Exception e) {
						System.out.println("Ups, something went wrong");
					}
				}	
			}
		}
		int regions = sc.nextInt();
		Game game = new Game();
	    game.sudoku = game.new Board(rows, columns, regions);
		game.sudoku.setValues(board);
		for (int i=0; i< regions;i++) {
			int num_cells = sc.nextInt();
			Game.Region new_region = game.new Region(num_cells);
			for (int j=0; j< num_cells; j++) {
				String cell = sc.next();
				String value1 = cell.substring(cell.indexOf("(") + 1, cell.indexOf(","));
				String value2 = cell.substring(cell.indexOf(",") + 1, cell.indexOf(")"));
				Game.Cell new_cell = game.new Cell(Integer.valueOf(value1)-1,Integer.valueOf(value2)-1);
				new_region.setCell(j, new_cell);
			}
			game.sudoku.setRegion(i, new_region);
		}
		int[][] answer = game.solver();
		for (int i=0; i<answer.length;i++) {
			for (int j=0; j<answer[0].length; j++) {
				System.out.print(answer[i][j]);
				if (j<answer[0].length -1) {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


}


