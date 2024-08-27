package documentExtractor;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DocumentExtractor {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        // image input and output paths
        String inputImagePath = "src/image_input/aadhar-example.jpg";
        String outputFolderPath = "src/image_output/";
        
        // Load the image
        Mat image = Imgcodecs.imread(inputImagePath);
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply adaptive thresholding
        Mat threshold = new Mat();
        Imgproc.adaptiveThreshold(gray, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);

        // Apply morphological operations to enhance edges
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_CLOSE, kernel);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter and crop documents
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            
            // Filter based on aspect ratio and size
            double aspectRatio = (double) rect.width / rect.height;
            if (rect.area() > image.width() * image.height() * 0.05 && 
                rect.area() < image.width() * image.height() * 0.5 &&
                aspectRatio > 0.5 && aspectRatio < 2.0 &&
                rect.y < image.rows() * 0.8) {
                
                Mat cropped = new Mat(image, rect);
                String outputFilePath = outputFolderPath + "cropped_" + rect.x + "_" + rect.y + ".jpg";
                Imgcodecs.imwrite(outputFilePath, cropped);
            }
        }
        
        System.out.println("Cropped Successfully");
    }
}
