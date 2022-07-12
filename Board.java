/* *****************************************************************************
 *  Name:              Ada Lovelace
 *  Coursera User ID:  123456
 *  Last modified:     October 16, 1842
 **************************************************************************** */

import edu.princeton.cs.algs4.StdRandom;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Board {

    private int[][] tiles;
    private final int n;
    private int blankSquare;

    // create a board from an n-by-n array of tiles,
    // where tiles[row][col] = tile at (row, col)
    public Board(int[][] tiles) {
        this.n = tiles.length;
        this.tiles = tiles;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (tiles[i][j] == 0) {
                    blankSquare = to1DimCoord(i, j);
                }
            }
        }
    }

    // string representation of this board
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(n);
        ret.append("\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ret.append("\s");
                ret.append(tiles[i][j]);
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    private int to1DimCoord(int row, int col) {
        return (n * row + col + 1);
    }

    private int[] to2DimCoord(int value) {
        int row = value % n == 0 ? (value / n) - 1 : (value / n);
        int col = value - n * row - 1;
        return new int[] {row, col};
    }

    private Board copy() {
        int[][] tilesCopy = new int[n][n];
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                tilesCopy[i][j] = tiles[i][j];
        return new Board(tilesCopy);
    }

    // board dimension n
    public int dimension() {
        return n;
    }

    // number of tiles out of place
    public int hamming() {
        int ret = 0;
        int maxValue = n * n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // last tile is for null
                if (to1DimCoord(i, j) == maxValue) break;
                int value = tiles[i][j];
                if (value == 0) {
                    if (i != (n - 1) || j != (n - 1)) {
                        ret += 1;
                    }
                    continue;
                }
                int[] coords = to2DimCoord(value);
                int dist = Math.abs(coords[0] - i) + Math.abs(coords[1] - j);
                if (dist != 0) {
                    ret += 1;
                }
            }
        }
        return ret;
    }

    // sum of Manhattan distances between tiles and goal
    public int manhattan() {
        int ret = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int value = tiles[i][j];
                if (value == 0) continue;
                int[] coords = to2DimCoord(value);
                int dist = Math.abs(coords[0] - i) + Math.abs(coords[1] - j);
                ret += dist;
            }
        }
        return ret;
    }

    // is this board the goal board?
    public boolean isGoal() {
        return hamming() == 0 && manhattan() == 0;
    }

    // does this board equal y?
    public boolean equals(Object y) {
        Board yBoard = (Board) y;
        if (this.n != yBoard.n) return false;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.tiles[i][j] != yBoard.tiles[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // all neighboring boards
    public Iterable<Board> neighbors() {
        return new Iterable<Board>() {
            public Iterator<Board> iterator() {
                return new BoardIterator();
            }
        };
    }

    private class BoardIterator implements Iterator<Board> {
        private int i;
        private Board[] boards;

        public BoardIterator() {
            int[] coords = to2DimCoord(blankSquare);
            int row = coords[0];
            int col = coords[1];
            boards = new Board[4];
            if (row == 0) {
                Board board = copy();
                // replace blank tile
                board.tiles[row][col] = board.tiles[row + 1][col];
                board.tiles[row + 1][col] = 0;
                boards[i++] = board;
            }
            else if (row == n - 1) {
                Board board = copy();
                // replace blank tile
                board.tiles[row][col] = board.tiles[row - 1][col];
                board.tiles[row - 1][col] = 0;
                boards[i++] = board;
            }
            else {
                Board boardBottom = copy();
                // replace blank tile
                boardBottom.tiles[row][col] = boardBottom.tiles[row + 1][col];
                boardBottom.tiles[row + 1][col] = 0;
                boards[i++] = boardBottom;

                Board boardTop = copy();
                boardTop.tiles[row][col] = boardTop.tiles[row - 1][col];
                boardTop.tiles[row - 1][col] = 0;
                boards[i++] = boardTop;
            }
            //
            if (col == 0) {
                Board board = copy();
                // replace blank tile
                board.tiles[row][col] = board.tiles[row][col + 1];
                board.tiles[row][col + 1] = 0;
                boards[i++] = board;
            }
            else if (col == n - 1) {
                Board board = copy();
                // replace blank tile
                board.tiles[row][col] = board.tiles[row][col - 1];
                board.tiles[row][col - 1] = 0;
                boards[i++] = board;
            }
            else {
                Board boardRight = copy();
                // replace blank tile
                boardRight.tiles[row][col] = boardRight.tiles[row][col + 1];
                boardRight.tiles[row][col + 1] = 0;
                boards[i++] = boardRight;

                Board boardLeft = copy();
                boardLeft.tiles[row][col] = boardLeft.tiles[row][col - 1];
                boardLeft.tiles[row][col - 1] = 0;
                boards[i++] = boardLeft;
            }
        }

        public boolean hasNext() {
            return i > 0;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Board next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Iterator is exhausted");
            }
            return boards[--i];
        }

    }

    // a board that is obtained by exchanging any pair of tiles
    public Board twin() {
        Board board = copy();
        int value = StdRandom.uniform(n*n - 1);
        int[] coords = to2DimCoord(value);
        int row = coords[0];
        int col = coords[1];
        int tmp = board.tiles[row][col];
        boolean changeRow = StdRandom.bernoulli();
        if (changeRow) {
            boolean up = false;
            if (row != 0 && row != n - 1) {
                up = StdRandom.bernoulli();
            }
            else if (row == n - 1) {
                up = true;
            }
            if (up) {
                board.tiles[row][col] = board.tiles[row - 1][col];
                board.tiles[row - 1][col] = tmp;
            }
            else {
                board.tiles[row][col] = board.tiles[row + 1][col];
                board.tiles[row + 1][col] = tmp;
            }

        }
        else {
            boolean left = false;
            if (col != 0 && col != n - 1) {
                left = StdRandom.bernoulli();
            }
            else if (col == n - 1) {
                left = true;
            }
            if (left) {
                board.tiles[row][col] = board.tiles[row][col - 1];
                board.tiles[row][col - 1] = tmp;
            }
            else {
                board.tiles[row][col] = board.tiles[row][col + 1];
                board.tiles[row][col + 1] = tmp;
            }
        }
        return board;
    }

    // unit testing (not graded)
    public static void main(String[] args) {
        int[][] tiles = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };
        Board board = new Board(tiles);
        System.out.println("Original: " + board);
        System.out.println("Twin: " + board.twin());
        Iterable<Board> neighbors = board.neighbors();
        for(Board b: neighbors){
             System.out.println("Neighbor: " + b.toString());
        }
    }
}
