package chess;
import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] pieces = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        pieces[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return pieces[position.getRow() -1][position.getColumn() -1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (ChessPiece[] row: pieces) {
            Arrays.fill(row, null);
        }

        ChessPiece.PieceType[] backRow = {
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT, 
            ChessPiece.PieceType.BISHOP, 
            ChessPiece.PieceType.QUEEN, 
            ChessPiece.PieceType.KING, 
            ChessPiece.PieceType.BISHOP, 
            ChessPiece.PieceType.KNIGHT, 
            ChessPiece.PieceType.ROOK
        };

        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(1, col), new ChessPiece(ChessGame.TeamColor.WHITE, backRow[col - 1]));
            addPiece(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(8, col), new ChessPiece(ChessGame.TeamColor.BLACK, backRow[col - 1]));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChessBoard)) {
            return false;
        }
        return Arrays.deepEquals(this.pieces, ((ChessBoard) obj).pieces);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pieces);
    }
}
