/* *****************************************************************************
 *  Name:              Ada Lovelace
 *  Coursera User ID:  123456
 *  Last modified:     October 16, 1842
 **************************************************************************** */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.StdOut;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Solver {
    public Board initial;
    public EnPriorityType priorityType = EnPriorityType.HAMMING;

    enum EnPriorityType {
        MANHATTAN,
        HAMMING
    }

    private class SearchNode implements Comparable<SearchNode> {
        // We define a search node of the game to be
        // a board,
        // the number of moves made to reach the board,
        // and the previous search node

        Board board;
        int moves;
        SearchNode prev;

        public SearchNode(Board board, int moves, SearchNode prev) {
            this.board = board;
            this.moves = moves;
            this.prev = prev;
        }

        public String toString() {
            StringBuilder ret = new StringBuilder();
            int priority = 0;
            if (priorityType.equals(EnPriorityType.MANHATTAN)) {
                ret.append("manhattan: " + board.manhattan() + "\n");
                priority = board.manhattan() + moves;
            }
            else {
                ret.append("hamming: " + board.hamming() + "\n");
                priority = board.hamming() + moves;
            }
            ret.append("moves: " + moves + "\n");
            ret.append("priority: " + priority + "\n");
            ret.append("board: " + board.toString());
            return ret.toString();
        }

        public int compareTo(SearchNode o) {

            if (priorityType.equals(EnPriorityType.MANHATTAN)) {
                int priority1 = this.board.manhattan() + this.moves;
                int priority2 = o.board.manhattan() + o.moves;

                if (priority1 < priority2) return -1;
                if (priority1 > priority2) return +1;
            }
            else if (priorityType.equals(EnPriorityType.HAMMING)) {
                int priority1 = this.board.hamming() + this.moves;
                int priority2 = o.board.hamming() + o.moves;

                if (priority1 > priority2) return +1;
                if (priority1 < priority2) return -1;
            }
            return 0;
        }
    }

    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial) {
        if (initial == null) throw new IllegalArgumentException();
        this.initial = initial;
    }

    private int countInversions(int[] tiles) {
        int boardSize = initial.dimension();
        int elementsNum = boardSize * boardSize;
        int invCount = 0;
        for (int i = 0; i < elementsNum - 1; i++) {
            for (int j = i + 1; j < elementsNum; j++) {
                if (tiles[j] != 0 && tiles[i] != 0 && tiles[i] > tiles[j]) {
                    invCount++;
                }
            }
        }
        return invCount;
    }

    // is the initial board solvable? (see below)
    public boolean isSolvable() {
        int N = initial.dimension();
        int[][] tiles2D = initial.getTiles();
        int[] tiles1D = new int[N * N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int idx = N * i + j;
                tiles1D[idx] = tiles2D[i][j];
            }
        }
        int invCount = countInversions(tiles1D);
        boolean invCountIsEven = invCount % 2 == 0;
        if (N % 2 == 1) {
            return invCountIsEven;
        }
        else {
            // get blank row from the bottom
            int blankRow = -1;
            for (int i = N - 1; i >= 0; i--) {
                for (int j = N - 1; j >= 0; j--) {
                    if (tiles2D[i][j] == 0)
                        blankRow = N - i;
                }
            }
            if (blankRow % 2 == 1) {
                return invCountIsEven;
            }
            return !invCountIsEven;
        }
    }

    // min number of moves to solve initial board; -1 if unsolvable
    public int moves() {
        if (!isSolvable()) {
            return -1;
        }
        int moves = 0;
        for (Board board : this.solution()) {
            moves++;
        }
        return moves;
    }

    // sequence of boards in a shortest solution; null if unsolvable
    public Iterable<Board> solution() {
        if (!isSolvable()) {
            return null;
        }
        return new Iterable<Board>() {
            public Iterator<Board> iterator() {
                return new ShortestSolutionBoards();
            }
        };
    }

    private class ShortestSolutionBoards implements Iterator<Board> {
        private int i;
        private int j = 0;
        private Board[] boards = {};
        public MinPQ<SearchNode> priorityQueue = new MinPQ<SearchNode>();

        public ShortestSolutionBoards() {
            priorityQueue.insert(new SearchNode(initial, 0, null));
            while (true) {
                SearchNode parentNode = priorityQueue.delMin();
                Board solution = parentNode.board;
                addBoard(solution);
                if (solution.isGoal()) {
                    break;
                }
                Board prevSearchNodeBoard = parentNode.prev == null ? null : parentNode.prev.board;
                int moves = parentNode.moves + 1;
                for(Board b: solution.neighbors()) {
                    // To reduce unnecessary exploration of useless search nodes,
                    // when considering the neighbors of a search node,
                    // don’t enqueue a neighbor if its board is the same as
                    // the board of the previous search node in the game tree.
                    if (prevSearchNodeBoard == null || !b.equals(prevSearchNodeBoard)) {
                        SearchNode childNode = new SearchNode(b, moves, parentNode);
                        priorityQueue.insert(childNode);
                    }
                }
            }
        }

        private void addBoard(Board board) {
            // repeated doubling
            if (i == boards.length) {
                int c = boards.length == 0? 1 : 0;
                resize(2 * (boards.length + c));
            }
            boards[i++] = board;
        }

        private void resize(int capacity) {
            Board[] copy = new Board[capacity];
            for (int k = 0; k < boards.length; k++) {
                copy[k] = boards[k];
            }
            boards = copy;
        }

        public boolean hasNext() {
            return j < i - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Board next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Iterator is exhausted");
            }
            return boards[++j];
        }

    }

    // test client (see below)
    public static void main(String[] args) {
        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tiles[i][j] = in.readInt();
        Board initial = new Board(tiles);
        // solve the puzzle
        Solver solver = new Solver(initial);
        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
