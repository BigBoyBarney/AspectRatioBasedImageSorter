import uk.co.jaimon.SimpleImageInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class CountInfo(
    var square: Int = 0,
    var portrait: Int = 0,
    var wide: Int = 0,
    var ultraWide: Int = 0,
    var moveBool: Boolean = true
)

var Info = CountInfo()
fun main() {
    val path = File(System.getProperty("user.dir"))
    val files = path.listFiles()
    val fileTypes = setOf("png", "jpg", "jpeg", "gif", "tiff", "bmp")
    countFiles(input=files, types=fileTypes)
    println("Make sure this file is in the same folder as your images.")
    moveOrNot()
    for (i in files.indices) {
        if ((files[i].isFile) and (files[i].extension in fileTypes)) {
            var x = aspectRatio(files[i])
            when{
                (0.9f <= x) and (x <= 1.1f) -> {
                    checkDirMove(dirname="Square", input=files[i])
                    Info.square++
                }
                (x<0.9f) -> {
                    checkDirMove(dirname="Portrait", input=files[i])
                    Info.portrait++
                }
                (1.1f < x) and (x <= 1.8f) -> {
                    checkDirMove(dirname="Wide", input=files[i])
                    Info.wide++
                }
                (1.8f < x) -> {
                    checkDirMove(dirname="Ultrawide", input=files[i])
                    Info.ultraWide++
                }
            }
            if (Info.moveBool==true) println("Moved " + files[i])
            else println("Copied" + files[i])
        }
    }
    println("Sorting complete. ${Info.square} square, ${Info.portrait} portrait, ${Info.wide} wide, ${Info.ultraWide} ultra-wide images were sorted.")
}

tailrec fun moveOrNot(){
    println("Would you like to copy or move the files? (copy / move)")
    when (readln()) {
        "copy" -> {
            Info.moveBool = false
            println("Copying, original files will be kept")
            return
        }
        "move" -> {
            Info.moveBool = true
            println("Moving, original files will not be kept")
            return
        }
        else -> {
            println("Please enter either 'move' or 'copy'")
            moveOrNot()
        }
    }
}

fun aspectRatio(input: File): Float {
    val imageInput = SimpleImageInfo(input)
    return imageInput.width.toFloat() / imageInput.height
}

fun checkDirMove(dirname: String, input: File){
    val directory = File(dirname)
    if (!directory.exists()) directory.mkdir()
    val sourcePath = Paths.get(input.path)
    val targetPath = Paths.get(System.getProperty("user.dir")+"\\"+dirname+"\\"+input.name)
    if(Info.moveBool)
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
    else Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
}

fun countFiles(input: Array<File>, types: Set<String>) {
    var imageCount = 0
    for (i in input.indices) {
        if (input[i].extension in types) imageCount++
    }
    println("$imageCount images found.")
}




