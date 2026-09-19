package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        List<ChessMove> moves = new ArrayList<>();

        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            ChessBoard testBoard = copyBoard(board);
            applyMove(testBoard, move);
        }
        return moves;
    }

    /**
     * Helper methods to copy chessboard, and test moves before actually making them.
     */

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                copy.addPiece(position, original.getPiece(position));
            }
        }
        return copy;
    }

    private void applyMove(ChessBoard targetBoard, ChessMove move) {
        ChessPiece piece = targetBoard.getPiece(move.getStartPosition());

        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece (piece.getTeamColor(), move.getPromotionPiece());
        }
        targetBoard.addPiece(move.getStartPosition(), null);
        targetBoard.addPiece(move.getEndPosition(), piece);
    }

    private boolean samePosition(ChessPosition first, ChessPosition second) {
    return first.getRow() == second.getRow()
            && first.getColumn() == second.getColumn();
    }

    private boolean sameMove(ChessMove first, ChessMove second) {
    return samePosition(first.getStartPosition(), second.getStartPosition())
            && samePosition(first.getEndPosition(), second.getEndPosition())
            && first.getPromotionPiece() == second.getPromotionPiece();
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return kingInCheck(board, teamColor);
    }

    private boolean kingInCheck (ChessBoard testBoard, TeamColor teamcolor) {
        ChessPosition kingPosition = null;

        for (int row = 1;row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = testBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamcolor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = position;
                }
            }
        }
        if (kingPosition == null) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = testBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamcolor) {
                    for (ChessMove move : piece.pieceMoves(testBoard, position)) {
                        if (samePosition(move.getEndPosition(), kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !legalMoves(teamColor);
    }

    private boolean legalMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (!moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !legalMoves(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
