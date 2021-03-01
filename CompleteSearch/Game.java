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

        public void remainingCellsAndValues(Board board, Region region, ArrayList<Cell> remainingCells, ArrayList<Integer> remainingValues) {
		final int max_num = region.getCells().length;

		remainingValues.ensureCapacity(max_num);
		remainingCells.addAll(Arrays.asList(region.getCells()));
		
		for (int i = 1; i <= max_num; ++i) {
			remainingValues.add(i);
		}
		
		for (final Cell c : region.getCells()) {
			if (remainingValues.remove((Object) board.getValue(c.getRow(), c.getColumn()))) {
				remainingCells.remove(c);
			}
		}
	}

        public HashSet<Integer> invalidValues(Board board, Region region, Cell cell, Cell[] indexToCell, HashMap<Cell, Region> cellToRegion) {
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
		for (final Cell siblingCell : region.getCells()) {
			final int siblingCellValue = board.getValue(siblingCell.getRow(), siblingCell.getColumn());
			if (cell != siblingCell && siblingCellValue != -1) invalidValues.add(siblingCellValue);
		}

		// later used for AOT invalid value
		final HashMap<Region, ArrayList<Cell>> neigboringRegionToCells = new HashMap<>();

		// do not use value of neighboring cells
		for (final int[] currDeltaIndices : deltaIndices) {
			final int otherRow = currDeltaIndices[0] + cell.getRow();
			final int otherColumn = currDeltaIndices[1] + cell.getColumn();

			if (
				otherRow >= 0 &&
				otherRow < board.num_rows &&
				otherColumn >= 0 &&
				otherColumn < board.num_columns
			) {
				final int otherCellValue = board.getValue(otherRow, otherColumn);
				if (otherCellValue != -1) invalidValues.add(board.getValue(otherRow, otherColumn));

				final Cell otherCell = indexToCell[otherColumn + otherRow * board.num_columns];
				final Region neighboringRegion = cellToRegion.get(otherCell);
				if (!neigboringRegionToCells.containsKey(neighboringRegion)) {
					neigboringRegionToCells.put(neighboringRegion, new ArrayList<>());
								}

				neigboringRegionToCells.get(neighboringRegion).add(otherCell);
			}
		}

		// verify if the cell value is valid based on another region's size and current state
		for (final Region r : neigboringRegionToCells.keySet()) {
			final ArrayList<Integer> remainingValues = new ArrayList<>();
			final ArrayList<Cell> remainingCells = new ArrayList<>();

			remainingCellsAndValues(board, r, remainingCells, remainingValues);

			remainingCells.retainAll(neigboringRegionToCells.get(r));

			if (remainingCells.size() == remainingValues.size()) {
				for (int v : remainingValues) invalidValues.add(v);
			}
		}

		return invalidValues;
	}

	// public ArrayList<Cell> solveByRemainingValues(Board board) {
	// 	final ArrayList<Cell> cellsChanged = new ArrayList<>(board.num_rows * board.num_columns * 2);

	// 	for (final Region r : board.getRegions()) {
	// 		final ArrayList<Integer> remainingValues = new ArrayList<>();
	// 		final ArrayList<Cell> remainingCells = new ArrayList<>();

	// 		remainingCellsAndValues(r, remainingCells, remainingValues);

	// 		if (remainingCells.size() == 1) {
	// 			final Cell c = remainingCells.get(0);
	// 			board.setValue(c.getRow(), c.getColumn(), remainingValues.get(0));
	// 			cellsChanged.add(c);
	// 		}
	// 	}

	// 	return cellsChanged;
	// }
	
	public int[][] solver() {
		//To Do => Please start coding your solution here
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


