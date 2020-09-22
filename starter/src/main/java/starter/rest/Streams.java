package starter.rest;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TIFFConstants;
import com.itextpdf.text.pdf.codec.TIFFDirectory;
import com.itextpdf.text.pdf.codec.TiffImage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import starter.service.Constant;
import starter.service.StreamService;
import starter.FileSizeLimit;
import starter.uContentException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "svc/", produces = MediaType.APPLICATION_JSON_VALUE)
public class Streams {

    @Autowired
    private StreamService streamService;

    @Autowired
    private FileSizeLimit fileSizeLimit;
    
    @RequestMapping(value = "{type}/{id}/_streamMetadata", method = RequestMethod.GET)
    public Object get(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder result = streamService.get(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "{type}/{id}/_streamMetadata/{streamId}", method = RequestMethod.GET)
    public Object get(@PathVariable String type, @PathVariable String id, @PathVariable String streamId) {
        try {
            XContentBuilder result = streamService.get(type, id, streamId);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/jpeg")
    public void geJpeg(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        getStream(type, id, streamId, response);
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/png")
    public void getPng(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        getStream(type, id, streamId, response);
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/gif")
    public void getGif(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        getStream(type, id, streamId, response);
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/tiff")
    public void getTiff(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, @RequestParam(defaultValue = "0") int pageIndex, HttpServletResponse response) {
        InputStream stream = null;
        try {
            Map<String, Object> result = streamService.getStream(type, id, streamId);
            stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
            reader.setInput(ImageIO.createImageInputStream(stream));
            int pageCount = reader.getNumImages(true);
            BufferedImage image = reader.read(pageIndex);
            if (image.getColorModel().getPixelSize() > 8) {
                response.setContentType("image/jpeg");
                ImageIO.write(image, "jpeg", response.getOutputStream());
            } else {
                response.setContentType("image/png");
                ImageIO.write(image, "png", response.getOutputStream());
            }

            // response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
            //stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            //IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);

        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=application/pdf")
    public void getPdf(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        try {
           final Rectangle a4 = new Rectangle(595, 842);
            Map<String, Object> result = streamService.getStream(type, id, streamId);
            String contentType = result.get(Constant.FieldName.CONTENTTYPE).toString();
            if (contentType.endsWith("pdf")) {
                response.setContentType(result.get(Constant.FieldName.CONTENTTYPE).toString());
                response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
                InputStream stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
                IOUtils.copy(stream, response.getOutputStream());
            } else if (contentType.endsWith("tif") || contentType.endsWith("tiff")) {
                RandomAccessFileOrArray tiffFile = new RandomAccessFileOrArray(new RandomAccessSourceFactory()
                        .setForceRead(false)
                        .setUsePlainRandomAccess(Document.plainRandomAccess)
                        .createSource((byte[]) result.get("bytes")));
                ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();

                ByteArrayInputStream arrayInputStream = new ByteArrayInputStream((byte[]) result.get("bytes"));
                reader.setInput(ImageIO.createImageInputStream(arrayInputStream));
                int numberOfPages = TiffImage.getNumberOfPages(tiffFile);
                Document document = new Document();
                PdfWriter pdfWriter = PdfWriter.getInstance(document, response.getOutputStream());
                pdfWriter.setStrictImageSequence(true);
                document.open();
                for (int i = 1; i <= numberOfPages; i++) {
                    Image img = null;
                    TIFFDirectory dir = new TIFFDirectory(tiffFile, i - 1);
                    if (dir.isTagPresent(TIFFConstants.TIFFTAG_TILEWIDTH)) {
                        BufferedImage image = reader.read(i - 1);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        if (image.getColorModel().getPixelSize() > 8) {
                            ImageIO.write(image, "jpeg", outputStream);
                        } else {
                            ImageIO.write(image, "png", outputStream);
                        }
                        img = Image.getInstance(outputStream.toByteArray());
                        outputStream.close();
                    } else {
                        img = TiffImage.getTiffImage(tiffFile, i);
                    }

                    img.scaleToFit(a4);
                    document.setPageSize(a4);
                    document.setMargins(0,0,0,0);
                   // document.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
                    document.newPage();
                    document.add(img);
                }
                document.close();
                arrayInputStream.close();
            } else if (contentType.endsWith("jpg") || contentType.endsWith("jpeg") || contentType.endsWith("png")) {
                Document document = new Document();
                PdfWriter pdfWriter = PdfWriter.getInstance(document, response.getOutputStream());
                pdfWriter.setStrictImageSequence(true);
                document.open();
                Image img = Image.getInstance((byte[]) result.get("bytes"));
                img.setAlignment(Image.MIDDLE);
                img.setPaddingTop(0);
                img.setScaleToFitLineWhenOverflow(true);
                document.setMargins(0, 0, 0, 0);
//                document.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
                img.scaleToFit(a4);
                document.setPageSize(a4);
                document.setMargins(0,0,0,0);
                document.newPage();
                document.add(img);
                document.close();
            } else {
                throw new uContentException("Unsupported Media Type", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取文档流
     * @param type 文档类型
     * @param id 文档id
     * @param streamId 文档流id
     * @param response
     */
    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    public void getStream(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        InputStream stream = null;
        try {
            Map<String, Object> result = streamService.getStream(type, id, streamId);
            response.setContentType(result.get(Constant.FieldName.CONTENTTYPE).toString());
            response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
            stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);

        }
    }


    @RequestMapping(value = "{type}/{id}/_stream/first", method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    public void getfirstStream(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
        InputStream stream = null;
        try {
            Map<String, Object> result = streamService.getStream(type, id, null);
            response.setContentType(result.get(Constant.FieldName.CONTENTTYPE).toString());
            response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
            stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);

        }
    }


    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.DELETE, consumes = "application/json")
    public Object delete(@PathVariable String type, @PathVariable String id, @RequestBody List<String> streamIds) {
        try {
            XContentBuilder result = streamService.delete(type, id, streamIds);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.POST, consumes = "multipart/*")
    public Object add(@PathVariable String type, @PathVariable String id,
                      @RequestParam(defaultValue = "0") Integer order,
                      MultipartHttpServletRequest request) {
        try {

            MultipartParser parser = new MultipartParser(request).invoke(fileSizeLimit);
            XContentBuilder result = streamService.add(type, id, order, parser.getFiles());
            return result.string();
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "_pdf", method = RequestMethod.GET)
    public void pdf(@RequestParam String path, HttpServletResponse response) {
        try {
            RandomAccessFileOrArray myTifFile = new RandomAccessFileOrArray(new RandomAccessSourceFactory()
                    .setForceRead(false)
                    .setUsePlainRandomAccess(Document.plainRandomAccess)
                    .createBestSource(path));

            ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
            reader.setInput(ImageIO.createImageInputStream(FileUtils.openInputStream(new File(path))));

            int numberOfPages = TiffImage.getNumberOfPages(myTifFile);
            Document tiffDocument = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(tiffDocument, response.getOutputStream());
            pdfWriter.setStrictImageSequence(true);
            tiffDocument.open();
            for (int i = 1; i <= numberOfPages; i++) {
                Image img = null;
                TIFFDirectory dir = new TIFFDirectory(myTifFile, i - 1);
                if (dir.isTagPresent(TIFFConstants.TIFFTAG_TILEWIDTH)) {
                    BufferedImage image = reader.read(i - 1);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    if (image.getColorModel().getPixelSize() > 8) {
                        ImageIO.write(image, "jpeg", outputStream);
                    } else {
                        ImageIO.write(image, "png", outputStream);
                    }
                    img = Image.getInstance(outputStream.toByteArray());
                    outputStream.close();
                } else {
                    img = TiffImage.getTiffImage(myTifFile, i);
                }
                Rectangle a4 = new Rectangle(595, 842); //150 ppi A4;
                img.scaleToFit(a4);

                //tiffDocument.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
                tiffDocument.setPageSize(a4);
                tiffDocument.newPage();
                tiffDocument.add(img);

            }
            tiffDocument.close();

        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

}
