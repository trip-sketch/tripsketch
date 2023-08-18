//package kr.kro.tripsketch.services
//
//@Service
//class FileService(private val mongoTemplate: MongoTemplate) {
//
//    fun upload(file: MultipartFile): String {
//        // 파일 업로드 로직
//        // ... (위의 FileUploadController의 파일 업로드 로직과 유사)
//
//        return "File uploaded successfully. File ID: ${savedFile.id}"
//    }
//
//    fun download(fileId: String, response: HttpServletResponse) {
//        // 파일 다운로드 로직
//        // ... (위의 FileDownloadController의 파일 다운로드 로직과 유사)
//    }
//
//    @PostMapping("/upload")
//    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
//        val filename = file.originalFilename ?: "file-${System.currentTimeMillis()}"
//        val fileContent = file.bytes
//        val contentType = file.contentType ?: "application/octet-stream"
//
//        val fileDocument = FileDocument(null, filename, contentType, fileContent)
//        val savedFile = mongoTemplate.save(fileDocument)
//
//        return "File uploaded successfully. File ID: ${savedFile.id}"
//    }
//
//    @GetMapping("/download/{fileId}")
//    fun downloadFile(@PathVariable fileId: String, response: HttpServletResponse) {
//        val fileDocument = mongoTemplate.findById(fileId, FileDocument::class.java)
//        if (fileDocument != null) {
//            response.contentType = fileDocument.contentType
//            response.setHeader("Content-Disposition", "attachment; filename=${fileDocument.filename}")
//            response.outputStream.write(fileDocument.content)
//            response.outputStream.flush()
//        } else {
//            response.status = HttpServletResponse.SC_NOT_FOUND
//        }
//    }
//}
