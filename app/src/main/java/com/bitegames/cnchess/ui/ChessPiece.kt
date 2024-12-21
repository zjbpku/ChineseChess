data class ChessPiece(
    val type: PieceType,
    val isRed: Boolean,
    var x: Int,
    var y: Int
)

enum class PieceType {
    KING,    // 将/帅
    ADVISOR, // 士
    ELEPHANT,// 象
    HORSE,   // 马
    ROOK,    // 车
    CANNON,  // 炮
    PAWN     // 兵/卒
} 