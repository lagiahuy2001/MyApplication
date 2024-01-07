package com.example.myapplication;
public class NQueenSolver {
    private int[][] chessBoard;
    private int n;
    private String result;
    private int queenCount;

    public NQueenSolver(int n) {
        this.n = n;
        this.chessBoard = new int[n][n];
        this.result = "";
        this.queenCount = 0;
    }

    private boolean isSafe(int row, int col) {
        for (int i = 0; i < col; i++)
            if (chessBoard[row][i] == 1)
                return false;

        for (int i = row, j = col; i >= 0 && j >= 0; i--, j--)
            if (chessBoard[i][j] == 1)
                return false;

        for (int i = row, j = col; i < n && j >= 0; i++, j--)
            if (chessBoard[i][j] == 1)
                return false;

        return true;
    }

    private boolean solve(int col) {
        if (col >= n)
            return true;

        for (int i = 0; i < n; i++) {
            if (isSafe(i, col)) {
                chessBoard[i][col] = 1;
                queenCount++;
                if (solve(col + 1))
                    return true;
                chessBoard[i][col] = 0; // backtrack
                queenCount--;
            }
        }

        return false;
    }

    public void solveNQueens() {
        if (!solve(0)) {
            result = "No solution exists\n";
            return;
        }
        printSolution();
    }

    private void printSolution() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                sb.append(chessBoard[i][j] + " ");
            sb.append("\n");
        }
        result = sb.toString();
    }

    public String getResult() {
        return result;
    }

    public int getQueenCount() {
        return queenCount;
    }
}
