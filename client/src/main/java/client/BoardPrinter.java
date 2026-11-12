package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;

/** Renders a readable starting board from either player's perspective. */
public final class BoardPrinter {
    private static final String LIGHT = "\u001b[48;5;223m";
    private static final String DARK = "\u001b[48;5;94m";
    private static final String WHITE_PIECE = "\u001b[38;5;160m";
    private static final String BLACK_PIECE = "\u001b[38;5;12m";
    private static final String RESET = "\u001b[0m";
    private static final String FILES = "abcdefgh";

    private BoardPrinter() { }

    public static void drawInitialBoard(ChessGame.TeamColor perspective, PrintStream out) {
        if (perspective == null || out == null) {
            throw new IllegalArgumentException("A board perspective and output stream are required.");
        }
        ChessBoard board = new ChessGame().getBoard();
        boolean blackPerspective = perspective == ChessGame.TeamColor.BLACK;

        out.println("    " + fileLabels(blackPerspective));
        for (int displayRow = 0; displayRow < 8; displayRow++) {
            int row = blackPerspective ? displayRow + 1 : 8 - displayRow;
            out.print(" " + row + "  ");
            for (int displayColumn = 0; displayColumn < 8; displayColumn++) {
                int column = blackPerspective ? 8 - displayColumn : displayColumn + 1;
                boolean lightSquare = (row + column) % 2 != 0;
                ChessPiece piece = board.getPiece(new ChessPosition(row, column));
                String glyph = piece == null ? "   " : " " + symbol(piece) + " ";
                String pieceColor = piece == null ? "" : piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? WHITE_PIECE : BLACK_PIECE;
                out.print((lightSquare ? LIGHT : DARK) + pieceColor + glyph + RESET);
            }
            out.println("  " + row);
        }
        out.println("    " + fileLabels(blackPerspective));
    }

    private static String fileLabels(boolean blackPerspective) {
        StringBuilder labels = new StringBuilder();
        for (int index = 0; index < 8; index++) {
            labels.append("  ").append(FILES.charAt(blackPerspective ? 7 - index : index)).append(' ');
        }
        return labels.toString();
    }

    private static char symbol(ChessPiece piece) {
        char letter = switch (piece.getPieceType()) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case ROOK -> 'R';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case PAWN -> 'P';
        };
        return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? letter : Character.toLowerCase(letter);
    }
}
