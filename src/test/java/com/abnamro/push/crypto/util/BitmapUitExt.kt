package com.abnamro.push.crypto.util

import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.io.File
import javax.imageio.ImageIO
import javax.swing.Spring.height

data class ImageData(val w: Int, val h: Int, val data: ByteArray, val type: Int)
fun File.readImage(): ImageData {
    val originalImage = ImageIO.read(this )
    val w = originalImage.width
    val h = originalImage.height
    val bf = originalImage.raster.dataBuffer as DataBufferByte

    val img = BufferedImage(w, h, originalImage.type)
    img.data = Raster.createRaster(img.sampleModel, DataBufferByte(bf.data, bf.size), Point(0, 0))
    val imgbf = img.data.dataBuffer as DataBufferByte
    return ImageData(w, h, imgbf.data, originalImage.type)
}

fun ByteArray.toBitmap(imageData: ImageData, dest: String){
    val img = BufferedImage(imageData.w, imageData.h, imageData.type)
    img.data = Raster.createRaster(img.sampleModel, DataBufferByte(this, this.size), null)
    ImageIO.write(img, "BMP", File(dest))
    println("result w ${img.width} h ${img.height}")

}