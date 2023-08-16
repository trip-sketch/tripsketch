package kr.kro.tripsketch.controllers

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/user")
class FileController(private val fileService: FileService) {

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
        return fileService.upload(file)
    }

    @GetMapping("/download/{fileId}")
    fun downloadFile(@PathVariable fileId: String, response: HttpServletResponse) {
        fileService.download(fileId, response)
    }
}