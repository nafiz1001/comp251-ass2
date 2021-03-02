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

        public void remainingCellsAndValues(int[][] values, Region region, ArrayList<Cell> remainingCells, ArrayList<Integer> remainingValues) {
		final int max_num = region.getCells().length;

		remainingValues.ensureCapacity(max_num);
		remainingCells.addAll(Arrays.asList(region.getCells()));
		
		for (int i = 1; i <= max_num; ++i) {
			remainingValues.add(i);
		}
		
		for (final Cell c : region.getCells()) {
			final Object cellValue = values[c.getRow()][c.getColumn()];
			if (remainingValues.remove(cellValue)) {
				remainingCells.remove(c);
			}
		}
	}

        public HashSet<Integer> invalidValues(int[][] values, Region region, Cell cell, Cell[][] coordToCell, HashMap<Cell, Region> cellToRegion) {
		final HashSet<Integer> invalidValues = new HashSet<>();

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

		// cannot contain value already present in the region
		for (final Cell otherCell : region.getCells()) {
			final int otherCellValue = values[otherCell.getRow()][otherCell.getColumn()];
			if (cell != otherCell && otherCellValue != -1) invalidValues.add(otherCellValue);
		}

		// later used for AOT invalid value
		final HashMap<Region, ArrayList<Cell>> neighboringRegionToCells = new HashMap<>();

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
				if (otherCellValue != -1) invalidValues.add(values[otherRow][otherColumn]);

				final Cell otherCell = coordToCell[otherRow][otherColumn];
				final Region neighboringRegion = cellToRegion.get(otherCell);
				if (!neighboringRegionToCells.containsKey(neighboringRegion)) {
					neighboringRegionToCells.put(neighboringRegion, new ArrayList<>());
				}

				neighboringRegionToCells.get(neighboringRegion).add(otherCell);
			}
		}

		// verify if the cell value is valid based on another region's size and current state
		for (final Region r : neighboringRegionToCells.keySet()) {
			final ArrayList<Integer> remainingValues = new ArrayList<>();
			final ArrayList<Cell> remainingCells = new ArrayList<>();

			remainingCellsAndValues(values, r, remainingCells, remainingValues);

			remainingCells.retainAll(neighboringRegionToCells.get(r));

			if (remainingCells.size() == remainingValues.size()) {
				for (int v : remainingValues) invalidValues.add(v);
			}
		}

		return invalidValues;
	}

	public void solveByObviousness(int[][] values) {
		for (final Region r : sudoku.getRegions()) {
			final ArrayList<Cell> remainingCells = new ArrayList<>();
			final ArrayList<Integer> remainingValues = new ArrayList<>();
			remainingCellsAndValues(values, r, remainingCells, remainingValues);

			if (remainingValues.size() == 1) {
				final Cell remainingCell = remainingCells.get(0);
				sudoku.setValue(remainingCell.getRow(), remainingCell.getColumn(), remainingValues.get(0));
			}
		}
	}

	public void printValues(int[][] answer) {
		for (int i=0; i<answer.length;i++) {
			for (int j=0; j<answer[0].length; j++) {
				System.out.print(answer[i][j]);
				if (j<answer[0].length -1) {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
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

	public int[][] solver_recurse(int[][] values, Cell[][] coordToCell, HashMap<Cell, Region> cellToRegion) {
		// initial progress
		solveByObviousness(values);

		for (final Region r : sudoku.getRegions()) {
			final ArrayList<Cell> remainingCells = new ArrayList<>();
			final ArrayList<Integer> remainingValues = new ArrayList<>();
			remainingCellsAndValues(values, r, remainingCells, remainingValues);
			
			for (final Cell c : remainingCells) {
				remainingValues.removeAll(invalidValues(values, r, c, coordToCell, cellToRegion));

				for (final Integer remainingValue : remainingValues) {
					final int[][] valuesCopy = copyValues(values);
					valuesCopy[c.row][c.column] = remainingValue;
					final int[][] solution = solver_recurse(valuesCopy, coordToCell, cellToRegion);

					if (solution != null) {
						return solution;
					}
				}
			}
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

		// initial progress
		solveByObviousness(sudoku.getValues());

		for (final Region r : sudoku.getRegions()) {
			final ArrayList<Cell> remainingCells = new ArrayList<>();
			final ArrayList<Integer> remainingValues = new ArrayList<>();
			remainingCellsAndValues(sudoku.getValues(), r, remainingCells, remainingValues);

			for (final Cell c : remainingCells) {
				remainingValues.removeAll(invalidValues(sudoku.getValues(), r, c, coordToCell, cellToRegion));

				for (final Integer remainingValue : remainingValues) {
					final int[][] valuesCopy = copyValues(sudoku.getValues());
					valuesCopy[c.row][c.column] = remainingValue;
					final int[][] solution = solver_recurse(valuesCopy, coordToCell, cellToRegion);

					if (solution != null) {
						sudoku.setValues(solution);
						return sudoku.getValues();
					}
				}
			}
		}

		return sudoku.getValues();
	}

	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
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
	}
	


}


