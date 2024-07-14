package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.util.lerp

internal sealed class PathAnimator :  ObjectAnimator<List<PathNode>, Path>()

internal class DynamicPathAnimator(
    override val duration: Float,
    override val valueFrom: List<PathNode>,
    override val valueTo: List<PathNode>,
    override val startOffset: Float,
    override val interpolator: Easing
) : PathAnimator() {

    init {
        require(valueFrom.size == valueTo.size)
    }

    private val path = Path()

    private val list = valueFrom.toMutableList()

    override fun interpolate(progress: Float): Path {
        repeat(list.size) {
            list[it] = lerp(valueFrom[it], valueTo[it], progress)
        }
        return list.toPath(path)
    }
}

internal class StaticPathAnimator(
    val value : List<PathNode>
) : PathAnimator() {
    override val startOffset: Float get() = 0f
    override val duration: Float get() = 0f
    override val valueFrom: List<PathNode> get() = value
    override val valueTo: List<PathNode> get() = value
    override val interpolator: Easing get() = LinearEasing

    private val path = Path()


    override fun interpolate(progress: Float): Path {
        return value.toPath(path)
    }
}


private fun lerp(from: PathNode, to: PathNode, fraction: Float): PathNode {

    return when (from) {
        PathNode.Close -> {
            to as PathNode.Close
            from
        }
        is PathNode.RelativeMoveTo -> {
            to as PathNode.RelativeMoveTo
            PathNode.RelativeMoveTo(
                lerp(from.dx, to.dx, fraction),
                lerp(from.dy, to.dy, fraction),
            )
        }
        is PathNode.MoveTo -> {
            to as PathNode.MoveTo
            PathNode.MoveTo(
                lerp(from.x, to.x, fraction),
                lerp(from.y, to.y, fraction),
            )
        }
        is PathNode.RelativeLineTo -> {
            to as PathNode.RelativeLineTo
            PathNode.RelativeLineTo(
                lerp(from.dx, to.dx, fraction),
                lerp(from.dy, to.dy, fraction),
            )
        }
        is PathNode.LineTo -> {
            to as PathNode.LineTo
            PathNode.LineTo(
                lerp(from.x, to.x, fraction),
                lerp(from.y, to.y, fraction),
            )
        }
        is PathNode.RelativeHorizontalTo -> {
            to as PathNode.RelativeHorizontalTo
            PathNode.RelativeHorizontalTo(
                lerp(from.dx, to.dx, fraction)
            )
        }
        is PathNode.HorizontalTo -> {
            to as PathNode.HorizontalTo
            PathNode.HorizontalTo(
                lerp(from.x, to.x, fraction)
            )
        }
        is PathNode.RelativeVerticalTo -> {
            to as PathNode.RelativeVerticalTo
            PathNode.RelativeVerticalTo(
                lerp(from.dy, to.dy, fraction)
            )
        }
        is PathNode.VerticalTo -> {
            to as PathNode.VerticalTo
            PathNode.VerticalTo(
                lerp(from.y, to.y, fraction)
            )
        }
        is PathNode.RelativeCurveTo -> {
            to as PathNode.RelativeCurveTo
            PathNode.RelativeCurveTo(
                lerp(from.dx1, to.dx1, fraction),
                lerp(from.dy1, to.dy1, fraction),
                lerp(from.dx2, to.dx2, fraction),
                lerp(from.dy2, to.dy2, fraction),
                lerp(from.dx3, to.dx3, fraction),
                lerp(from.dy3, to.dy3, fraction),
            )
        }
        is PathNode.CurveTo -> {
            to as PathNode.CurveTo
            PathNode.CurveTo(
                lerp(from.x1, to.x1, fraction),
                lerp(from.y1, to.y1, fraction),
                lerp(from.x2, to.x2, fraction),
                lerp(from.y2, to.y2, fraction),
                lerp(from.x3, to.x3, fraction),
                lerp(from.y3, to.y3, fraction),
            )
        }
        is PathNode.RelativeReflectiveCurveTo -> {
            to as PathNode.RelativeReflectiveCurveTo
            PathNode.RelativeReflectiveCurveTo(
                lerp(from.dx1, to.dx1, fraction),
                lerp(from.dy1, to.dy1, fraction),
                lerp(from.dx2, to.dx2, fraction),
                lerp(from.dy2, to.dy2, fraction),
            )
        }
        is PathNode.ReflectiveCurveTo -> {
            to as PathNode.ReflectiveCurveTo
            PathNode.ReflectiveCurveTo(
                lerp(from.x1, to.x1, fraction),
                lerp(from.y1, to.y1, fraction),
                lerp(from.x2, to.x2, fraction),
                lerp(from.y2, to.y2, fraction),
            )
        }
        is PathNode.RelativeQuadTo -> {
            to as PathNode.RelativeQuadTo
            PathNode.RelativeQuadTo(
                lerp(from.dx1, to.dx1, fraction),
                lerp(from.dy1, to.dy1, fraction),
                lerp(from.dx2, to.dx2, fraction),
                lerp(from.dy2, to.dy2, fraction),
            )
        }
        is PathNode.QuadTo -> {
            to as PathNode.QuadTo
            PathNode.QuadTo(
                lerp(from.x1, to.x1, fraction),
                lerp(from.y1, to.y1, fraction),
                lerp(from.x2, to.x2, fraction),
                lerp(from.y2, to.y2, fraction),
            )
        }
        is PathNode.RelativeReflectiveQuadTo -> {
            to as PathNode.RelativeReflectiveQuadTo
            PathNode.RelativeReflectiveQuadTo(
                lerp(from.dx, to.dx, fraction),
                lerp(from.dy, to.dy, fraction),
            )
        }
        is PathNode.ReflectiveQuadTo -> {
            to as PathNode.ReflectiveQuadTo
            PathNode.ReflectiveQuadTo(
                lerp(from.x, to.x, fraction),
                lerp(from.y, to.y, fraction),
            )
        }
        is PathNode.RelativeArcTo -> {
            to as PathNode.RelativeArcTo

            PathNode.RelativeArcTo(
                horizontalEllipseRadius = lerp(from.horizontalEllipseRadius, to.horizontalEllipseRadius, fraction),
                verticalEllipseRadius =  lerp(from.verticalEllipseRadius, to.verticalEllipseRadius, fraction),
                theta = lerp(from.theta, to.theta, fraction),
                isMoreThanHalf = from.isMoreThanHalf || to.isMoreThanHalf,
                isPositiveArc = from.isPositiveArc || to.isPositiveArc,
                arcStartDx =  lerp(from.arcStartDx, to.arcStartDx, fraction),
                arcStartDy =  lerp(from.arcStartDy, to.arcStartDy, fraction),
            )
        }
        is PathNode.ArcTo -> {
            to as PathNode.ArcTo

            PathNode.ArcTo(
                horizontalEllipseRadius = lerp(from.horizontalEllipseRadius, to.horizontalEllipseRadius, fraction),
                verticalEllipseRadius =  lerp(from.verticalEllipseRadius, to.verticalEllipseRadius, fraction),
                theta = lerp(from.theta, to.theta, fraction),
                isMoreThanHalf = from.isMoreThanHalf || to.isMoreThanHalf,
                isPositiveArc = from.isPositiveArc || to.isPositiveArc,
                arcStartX =  lerp(from.arcStartX, to.arcStartX, fraction),
                arcStartY =  lerp(from.arcStartY, to.arcStartY, fraction),
            )
        }
    }
}