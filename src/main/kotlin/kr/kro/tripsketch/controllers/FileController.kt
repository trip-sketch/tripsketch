package kr.kro.tripsketch.controllers

@RestController
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