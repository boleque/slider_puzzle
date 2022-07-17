/* *****************************************************************************
 *  Name:              Ada Lovelace
 *  Coursera User ID:  123456
 *  Last modified:     October 16, 1842
 **************************************************************************** */

import edu.princeton.cs.algs4.MinPQ;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Solver {
    public MinPQ<SearchNode> priorityQueue = new MinPQ<SearchNode>();
    public Board initial;
    public EnPriorityType priorityType = EnPriorityType.MANHATTAN;

    enum EnPriorityType {
        MANHATTAN,
        HAMMING
    }

    public class SearchNode {
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

        public Comparator<SearchNode> hammingPriority() {
            return new Comparator<SearchNode>() {
                public int compare(SearchNode sn1, SearchNode sn2) {
                    int priority1 = sn1.board.hamming() + sn1.moves;
                    int priority2 = sn2.board.hamming() + sn2.moves;

                    if (priority1 > priority2) return +1;
                    if (priority1 < priority2) return -1;
                    return 0;
                }
            };
        }

        public Comparator<SearchNode> manhattanPriorit() {
            return new Comparator<SearchNode>() {
                public int compare(SearchNode sn1, SearchNode sn2) {
                    int priority1 = sn1.board.manhattan() + sn1.moves;
                    int priority2 = sn2.board.manhattan() + sn2.moves;

                    if (priority1 > priority2) return -1;
                    if (priority1 < priority2) return +1;
                    return 0;
                }
            };
        }
    }

    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial, EnPriorityType priorityType) {
        if (initial == null) throw new IllegalArgumentException();
        this.initial = initial;
        this.priorityType = priorityType;
        priorityQueue.insert(new SearchNode(initial, 0, null));
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

    // Return -1 in moves() if the board is unsolvable.
    // Return null in solution() if the board is unsolvable.

    // min number of moves to solve initial board; -1 if unsolvable
    public int moves() {
        if (!isSolvable()) {
            return -1;
        }

        return 1;
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
        private Board[] boards;

        public ShortestSolutionBoards() {
            while (true) {
                SearchNode parentNode = priorityQueue.delMin();
                addBoard(parentNode.board);
                if (parentNode.board.isGoal()) {
                    break;
                }
                Board prevSearchNodeBoard = parentNode.prev == null ? null : parentNode.prev.board;
                int moves = parentNode.moves + 1;
                for(Board b: parentNode.board.neighbors()) {
                    // To reduce unnecessary exploration of useless search nodes,
                    // when considering the neighbors of a search node,
                    // don’t enqueue a neighbor if its board is the same as
                    // the board of the previous search node in the game tree.
                    if (!b.equals(prevSearchNodeBoard)) {
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
            for (int i = 0; i < boards.length; i++) {
                copy[i] = boards[i];
            }
            boards = copy;
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

    // test client (see below)
    public static void main(String[] args) {
        int[][] tiles = {
                {0,1,3},
                {4,2,5},
                {7,8,6},
        };

        Board board = new Board(tiles);
        Board twinBoard = board.twin();

        Solver solver = new Solver(board, EnPriorityType.MANHATTAN);
        Solver twinSolver = new Solver(twinBoard, EnPriorityType.MANHATTAN);

        System.out.println("The board solvable -> " + solver.isSolvable());
        System.out.println("The twin is unsolvable -> " + !twinSolver.isSolvable());
    }
}
