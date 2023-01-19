package io.github.g0dkar.qrcode.render

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.files.Blob

@JsExport
@OptIn(ExperimentalJsExport::class)
@Suppress("MemberVisibilityCanBePrivate")
actual open class QRCodeGraphics actual constructor(
    val width: Int,
    val height: Int
) {
    companion object {
        private const val CANVAS_UNSUPPORTED = "Canvas seems to not be supported :("
    }

    private val canvas: HTMLCanvasElement
    private val context: CanvasRenderingContext2D

    init {
        val canvas = tryGet { document.createElement("canvas") as HTMLCanvasElement }

        canvas.width = width
        canvas.height = height

        val context = tryGet { canvas.getContext("2d") as CanvasRenderingContext2D }

        this.canvas = canvas
        this.context = context
    }

    private fun rgba(color: Int): String {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = (color shr 0) and 0xFF
        val a = ((color shr 24) and 0xFF) / 255.0
        return "rgba($r,$g,$b,$a)"
    }

    private fun draw(color: Int, action: () -> Unit) {
        val colorString = rgba(color)
        context.fillStyle = colorString
        context.strokeStyle = colorString
        action()
    }

    /**
     * Returns a Data URL to this can be shown in an `<img/>` tag.
     */
    open fun toDataURL(format: String = "png"): String = canvas.toDataURL(format)

    /**
     * Direct access to the `.toBlob()` function of the underlying canvas.
     *
     * Syntactic sugar for `nativeImage().toBlob(callback)`.
     */
    open fun toBlob(callback: (Blob?) -> Unit): Unit = canvas.toBlob(callback)

    /** Returns this image as a [ByteArray] encoded as PNG. */
    actual open fun getBytes(): ByteArray = getBytes("png")

    /** Returns this image as a [ByteArray] encoded as the specified format (e.g. `PNG`, `JPG`, `BMP`, ...). */
    @JsName("getBytesForFormat")
    actual open fun getBytes(format: String): ByteArray =
        canvas.toDataURL(format).encodeToByteArray()

    /** Returns the available formats to be passed as parameters to [getBytes].
     *
     * **Note:** The actual list of supported formats depends on the browser, so this won't be checked. PNG is always supported.
     */
    actual open fun availableFormats(): Array<String> = arrayOf("png")

    /** Returns the native image object this QRCodeGraphics is working upon. */
    actual open fun nativeImage(): Any = canvas

    /** Draw a straight line from point `(x1,y1)` to `(x2,y2)`. */
    actual open fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        draw(color) {
            context.moveTo(x1.toDouble(), y1.toDouble())
            context.lineTo(x2.toDouble(), y2.toDouble())
        }
    }

    /** Draw the edges of a rectangle starting at point `(x,y)` and having `width` by `height`. */
    actual open fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int) {
        draw(color) {
            context.strokeRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }
    }

    /** Fills the rectangle starting at point `(x,y)` and having `width` by `height`. */
    actual open fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Int) {
        draw(color) {
            context.fillRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }
    }

    /** Fill the whole area of this canvas with the specified [color]. */
    actual open fun fill(color: Int) {
        fillRect(0, 0, width, height, color)
    }

    /**
     * Draw the edges of a round rectangle starting at point `(x,y)` and having `width` by `height`
     * with edges that are `borderRadius` pixels round (almost like CSS).
     *
     * If it helps, these would _in theory_ draw the same thing:
     *
     * ```
     * // CSS
     * .roundRect {
     *     width: 100px;
     *     height: 100px;
     *     border-radius: 5px;
     * }
     *
     * // Kotlin
     * drawRoundRect(0, 0, 100, 100, 5)
     * ```
     *
     * **Note:** you can't specify different sizes for different edges. This is just an example :)
     *
     */
    actual open fun drawRoundRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        borderRadius: Int,
        color: Int
    ) {
        drawRect(x, y, width, height, color)
    }

    /**
     * Fills the round rectangle starting at point `(x,y)` and having `width` by `height`
     * with edges that are `borderRadius` pixels round (almost like CSS).
     *
     * If it helps, these would _in theory_ draw the same thing:
     *
     * ```
     * // CSS
     * .roundRect {
     *     width: 100px;
     *     height: 100px;
     *     border-radius: 5px;
     * }
     *
     * // Kotlin
     * drawRoundRect(0, 0, 100, 100, 5)
     * ```
     *
     * **Note:** you can't specify different sizes for different edges. This is just an example :)
     *
     */
    actual open fun fillRoundRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        borderRadius: Int,
        color: Int
    ) {
        fillRect(x, y, width, height, color)
    }

    /** Draw an image inside another. Mostly used to merge squares into the main QRCode. */
    actual open fun drawImage(img: QRCodeGraphics, x: Int, y: Int) {
        context.drawImage(img.canvas, x.toDouble(), y.toDouble())
    }

    private fun <T> tryGet(what: () -> T): T =
        try {
            what()
        } catch (t: Throwable) {
            throw Error(CANVAS_UNSUPPORTED, cause = t)
        }
}