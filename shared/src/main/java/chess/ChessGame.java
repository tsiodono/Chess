package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame 
{

    private ChessBoard board;
    private TeamColor teamTurn;

    private ChessMove lastMove;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean whiteRookAMoved;
    private boolean whiteRookHMoved;
    private boolean blackRookAMoved;
    private boolean blackRookHMoved;


    public ChessGame() 
    {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;

        lastMove = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() 
    {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) 
    {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor 
    {
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
    public Collection<ChessMove> validMoves(ChessPosition startPosition)
    {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) 
        {
            return null;
        }

        List<ChessMove> moves = new ArrayList<>();

        for (ChessMove move : piece.pieceMoves(board, startPosition)) 
        {
            ChessBoard testBoard = copyBoard(board);
            applyMove(testBoard, move);

            if (!kingInCheck(testBoard, piece.getTeamColor()))
            {
                moves.add(move);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING)
        {
            for (ChessMove castle : castlingMoves(startPosition, piece))
            {
                ChessBoard testBoard = copyBoard(board);
                applyMove(testBoard, castle);
                if (!kingInCheck(testBoard, piece.getTeamColor()))
                {
                    moves.add(castle);
                }
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN)
        {
            for (ChessMove enPassant : enPassantMoves(startPosition, piece))
            {
                ChessBoard testBoard = copyBoard(board);
                applyMove(testBoard, enPassant);
                if (!kingInCheck(testBoard, piece.getTeamColor()))
                {
                    moves.add(enPassant);
                }
            }
        }

        return moves;
    }

    /**
     * Helper methods to copy chessboard, and test moves before actually making them.
     */

    private ChessBoard copyBoard(ChessBoard original) 
    {
        ChessBoard copy = new ChessBoard();

        for (int row = 1; row <= 8; row++) 
        {
            for (int col = 1; col <= 8; col++) 
            {
                ChessPosition position = new ChessPosition(row, col);
                copy.addPiece(position, original.getPiece(position));
            }
        }
        return copy;
    }

    private void applyMove(ChessBoard targetBoard, ChessMove move) 
    {
        ChessPiece piece = targetBoard.getPiece(move.getStartPosition());

        boolean castling = isCastlingMove(targetBoard, move);
        boolean enPassant = isEnPassantMove(targetBoard, move);

        if (move.getPromotionPiece() != null) 
        {
            piece = new ChessPiece (piece.getTeamColor(), move.getPromotionPiece());
        }
        targetBoard.addPiece(move.getStartPosition(), null);
        targetBoard.addPiece(move.getEndPosition(), piece);

        if (enPassant)
        {
            targetBoard.addPiece(new ChessPosition(
                    move.getStartPosition().getRow(),
                    move.getEndPosition().getColumn()), null);
        }

        if (castling)
        {
            int row = move.getStartPosition().getRow();
            boolean kingSide = move.getEndPosition().getColumn()
                    > move.getStartPosition().getColumn();
            int rookStartColumn = kingSide ? 8 : 1;
            int rookEndColumn = kingSide ? 6 : 4;
            ChessPosition rookStart = new ChessPosition(row, rookStartColumn);
            ChessPosition rookEnd = new ChessPosition(row, rookEndColumn);
            ChessPiece rook = targetBoard.getPiece(rookStart);
            targetBoard.addPiece(rookStart, null);
            targetBoard.addPiece(rookEnd, rook);
        }
    }

    private Collection<ChessMove> castlingMoves(ChessPosition startPosition, ChessPiece king)
    {
        List<ChessMove> moves = new ArrayList<>();
        int row = king.getTeamColor() == TeamColor.WHITE ? 1 : 8;

        if (startPosition.getRow() != row || startPosition.getColumn() != 5
                || hasKingMoved(king.getTeamColor())
                || kingInCheck(board, king.getTeamColor()))
        {
            return moves;
        }

        addCastleIfLegal(moves, row, 1, 3, king.getTeamColor());
        addCastleIfLegal(moves, row, 8, 7, king.getTeamColor());
        return moves;
    }

    private void addCastleIfLegal(List<ChessMove> moves, int row, int rookColumn,int kingDestinationColumn, TeamColor teamColor)
    {
        if (rookHasMoved(teamColor, rookColumn))
        {
            return;
        }

        ChessPiece rook = board.getPiece(new ChessPosition(row, rookColumn));
        if (rook == null || rook.getPieceType() != ChessPiece.PieceType.ROOK || rook.getTeamColor() != teamColor)
        {
            return;
        }

        int direction = Integer.compare(kingDestinationColumn, 5);
        for (int column = 5 + direction; column != rookColumn; column += direction)
        {
            if (board.getPiece(new ChessPosition(row, column)) != null)
            {
                return;
            }
        }

        ChessPosition kingStart = new ChessPosition(row, 5);
        ChessPosition kingTransit = new ChessPosition(row, 5 + direction);
        ChessBoard transitBoard = copyBoard(board);
        applyMove(transitBoard, new ChessMove(kingStart, kingTransit, null));
        if (kingInCheck(transitBoard, teamColor))
        {
            return;
        }

        ChessMove castle = new ChessMove(
                kingStart, new ChessPosition(row, kingDestinationColumn), null);
        ChessBoard destinationBoard = copyBoard(board);
        applyMove(destinationBoard, castle);
        if (!kingInCheck(destinationBoard, teamColor))
        {
            moves.add(castle);
        }
    }

    private boolean hasKingMoved(TeamColor teamColor)
    {
        return teamColor == TeamColor.WHITE ? whiteKingMoved : blackKingMoved;
    }

    private boolean rookHasMoved(TeamColor teamColor, int rookColumn)
    {
        if (teamColor == TeamColor.WHITE)
        {
            return rookColumn == 1 ? whiteRookAMoved : whiteRookHMoved;
        }
        return rookColumn == 1 ? blackRookAMoved : blackRookHMoved;
    }

    private Collection<ChessMove> enPassantMoves(ChessPosition startPosition, ChessPiece pawn)
    {
        List<ChessMove> moves = new ArrayList<>();
        if (lastMove == null)
        {
            return moves;
        }

        int direction = pawn.getTeamColor() == TeamColor.WHITE ? 1 : -1;
        int requiredRow = pawn.getTeamColor() == TeamColor.WHITE ? 5 : 4;
        if (startPosition.getRow() != requiredRow)
        {
            return moves;
        }

        for (int columnDelta : new int[]{-1, 1})
        {
            int destinationColumn = startPosition.getColumn() + columnDelta;
            if (destinationColumn < 1 || destinationColumn > 8)
            {
                continue;
            }

            ChessMove move = new ChessMove(startPosition,
                    new ChessPosition(startPosition.getRow() + direction, destinationColumn), null);
            if (isEnPassantMove(board, move))
            {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean isCastlingMove(ChessBoard targetBoard, ChessMove move)
    {
        ChessPiece piece = targetBoard.getPiece(move.getStartPosition());
        return piece != null
                && piece.getPieceType() == ChessPiece.PieceType.KING
                && move.getStartPosition().getRow() == move.getEndPosition().getRow()
                && Math.abs(move.getEndPosition().getColumn()
                        - move.getStartPosition().getColumn()) == 2;
    }

    private boolean isEnPassantMove(ChessBoard targetBoard, ChessMove move)
    {
        if (lastMove == null)
        {
            return false;
        }

        ChessPiece pawn = targetBoard.getPiece(move.getStartPosition());
        if (pawn == null || pawn.getPieceType() != ChessPiece.PieceType.PAWN
                || targetBoard.getPiece(move.getEndPosition()) != null)
        {
            return false;
        }

        int direction = pawn.getTeamColor() == TeamColor.WHITE ? 1 : -1;
        if (move.getEndPosition().getRow() - move.getStartPosition().getRow() != direction
                || Math.abs(move.getEndPosition().getColumn()
                        - move.getStartPosition().getColumn()) != 1)
        {
            return false;
        }

        ChessPosition adjacentPosition = new ChessPosition(
                move.getStartPosition().getRow(), move.getEndPosition().getColumn());
        ChessPiece adjacentPiece = targetBoard.getPiece(adjacentPosition);
        if (adjacentPiece == null || adjacentPiece.getPieceType() != ChessPiece.PieceType.PAWN
                || adjacentPiece.getTeamColor() == pawn.getTeamColor())
        {
            return false;
        }

        return samePosition(lastMove.getEndPosition(), adjacentPosition)
                && lastMove.getStartPosition().getColumn()
                        == lastMove.getEndPosition().getColumn()
                && Math.abs(lastMove.getEndPosition().getRow()
                        - lastMove.getStartPosition().getRow()) == 2
                && (lastMove.getStartPosition().getRow() == 2
                        || lastMove.getStartPosition().getRow() == 7);
    }

    private void updateCastlingRights(ChessMove move, ChessPiece movingPiece)
    {
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING)
        {
            if (movingPiece.getTeamColor() == TeamColor.WHITE)
            {
                whiteKingMoved = true;
            }
            else
            {
                blackKingMoved = true;
            }
        }

        if (movingPiece.getPieceType() == ChessPiece.PieceType.ROOK)
        {
            markRookMoved(movingPiece.getTeamColor(), move.getStartPosition());
        }

        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
        if (capturedPiece != null && capturedPiece.getPieceType() == ChessPiece.PieceType.ROOK)
        {
            markRookMoved(capturedPiece.getTeamColor(), move.getEndPosition());
        }
    }

    private void markRookMoved(TeamColor teamColor, ChessPosition position)
    {
        if (teamColor == TeamColor.WHITE && position.getRow() == 1)
        {
            if (position.getColumn() == 1) whiteRookAMoved = true;
            if (position.getColumn() == 8) whiteRookHMoved = true;
        }
        if (teamColor == TeamColor.BLACK && position.getRow() == 8)
        {
            if (position.getColumn() == 1) blackRookAMoved = true;
            if (position.getColumn() == 8) blackRookHMoved = true;
        }
    }

    private boolean samePosition(ChessPosition first, ChessPosition second) 
    {
        return first.getRow() == second.getRow()
                && first.getColumn() == second.getColumn();
    }

    private boolean sameMove(ChessMove first, ChessMove second) 
    {
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
    public void makeMove(ChessMove move) throws InvalidMoveException 
    {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) 
        {
            throw new InvalidMoveException("There is no piece at the start position");
        }

        if (piece.getTeamColor() != teamTurn) 
        {
            throw new InvalidMoveException("It is not this piece's turn");
        }

        Collection<ChessMove> moves = validMoves(move.getStartPosition());

        boolean legal = false;
        for (ChessMove legalMove : moves) 
        {
            if (sameMove(legalMove, move)) 
            {
                legal = true;
                break;
            }
        }

        if (!legal) 
        {
            throw new InvalidMoveException("Invalid move");
        }

        updateCastlingRights(move, piece);
        applyMove(board, move);
        lastMove = move;

        teamTurn = teamTurn == TeamColor.WHITE
                ? TeamColor.BLACK
                : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) 
    {
        return kingInCheck(board, teamColor);
    }

    private boolean kingInCheck (ChessBoard testBoard, TeamColor teamcolor) 
    {
        ChessPosition kingPosition = null;

        for (int row = 1;row <= 8; row++) 
        {
            for (int col = 1; col <= 8; col++) 
            {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = testBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamcolor && piece.getPieceType() == ChessPiece.PieceType.KING) 
                {
                    kingPosition = position;
                }
            }
        }
        if (kingPosition == null) 
        {
            return false;
        }
        for (int row = 1; row <= 8; row++) 
        {
            for (int col = 1; col <= 8; col++) 
            {
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = testBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamcolor) 
                {
                    for (ChessMove move : piece.pieceMoves(testBoard, position))
                    {
                        if (samePosition(move.getEndPosition(), kingPosition)) 
                        {
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
    public boolean isInCheckmate(TeamColor teamColor)
    {
        return isInCheck(teamColor) && !legalMoves(teamColor);
    }

    private boolean legalMoves(TeamColor teamColor)
    {
        for (int row = 1; row <= 8; row++) 
        {
            for (int col = 1; col <= 8; col++) 
            {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) 
                {
                    Collection<ChessMove> moves = validMoves(position);
                    if (!moves.isEmpty()) 
                    {
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
    public boolean isInStalemate(TeamColor teamColor)
    {
        return !isInCheck(teamColor) && !legalMoves(teamColor);
    }
    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) 
    {
        this.board = board;
        lastMove = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() 
    {
        return this.board;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ChessGame other))
        {
            return false;
        }
        return Objects.equals(this.board, other.board)
                && this.teamTurn == other.teamTurn;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.board, this.teamTurn);
    }
}
