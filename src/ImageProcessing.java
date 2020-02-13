import java.util.List;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {
	
	protected static ArrayList<ParkingSpot> emptyParkingLot = new ArrayList<ParkingSpot>();
	protected ArrayList<ParkingSpot> cars = new ArrayList<ParkingSpot>();
	Mat parkingMask;
	int num = 1;
	String im;
	int spotsAvailable = 0;


	public void emptyParkingLotProcessing(Mat source) {
		spotsAvailable = 0;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		Mat contourDest = Mat.zeros(destination.size(), CvType.CV_8UC3);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		parkingMask = destination;
		Mat s2 = Imgcodecs.imread(im);
		// FILTER EMPTY PARKING LOT
		// GRAYSCALE
		Imgproc.cvtColor(source, destination, Imgproc.COLOR_RGB2GRAY);
		// BINARY THE IMAGE BLACK AND WHITE
		Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
		// THICKEN AND SMOTTH THE LINES
		Imgproc.dilate(destination, destination, new Mat(), new Point(-1, 1), 2);
		// FIND CONTOURS
		Imgproc.findContours(destination.clone(), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(contourDest, contours, -1, new Scalar(255, 255, 255));
		
		// POPULATE emptyParkingLot WITH PARKING SPACES FROM CONTOURS
		for(int i = 0; i < contours.size(); i++){
			Rect r = Imgproc.boundingRect(contours.get(i));
			if(r.area() >= 2000 && ((r.height / (double) r.width) > 1.0)) {
				Imgproc.rectangle(s2, r.tl(), r.br(), new Scalar(0, 255, 0), -1);
				
				ParkingSpot p = new ParkingSpot(r);
				p.setSpotNumber(num);
				num++;
				emptyParkingLot.add(p);
			}
		}

	    Imgcodecs.imwrite("rsc/firstResult.png", s2);
	}
	
	// RETURNS A MAT OF THE IMAGE WITH PARKING LINES CONNECTED
	public Mat connectPoints(String image) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat source = Imgcodecs.imread(image);
		Mat sourceClone = source.clone();
		im = image;
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		Mat contourDest = Mat.zeros(destination.size(), CvType.CV_8UC3);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		// FILTER THE IMAGE
		// GRAYSCALE
		Imgproc.cvtColor(source, destination, Imgproc.COLOR_RGB2GRAY);
		// BINARY THE IMAGE TO BLACK AND WHITE
		Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
		// THICKEN AND SMOOTH THE LINES
		Imgproc.dilate(destination, destination, new Mat(), new Point(-1, 1), 2);


		// FIND CONTOURS
		Imgproc.findContours(destination.clone(), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(contourDest, contours, -1, new Scalar(255, 255, 255));
		for(int i = 0; i < contours.size(); i++){
			Rect r = Imgproc.boundingRect(contours.get(i));
			// LINES THAT CONNECT RECTANGLE POINTS
			// ADDS LINES TO THE ORIGINAL IMAGE
			Imgproc.line(sourceClone, new Point(r.br().x - 5, r.tl().y), new Point(r.tl().x + 5, r.tl().y), new Scalar(255, 255, 255), 6);
			Imgproc.line(sourceClone, new Point(r.tl().x + 5, r.br().y), new Point(r.br().x - 5, r.br().y), new Scalar(255, 255, 255), 6);
		}
	    return sourceClone;
	}
	

	public void activeParkingLotProcessing(String source2) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat source = Imgcodecs.imread(source2);
		Mat destination = new Mat(source.rows(), source.cols(), source.type());
		Mat contourDest = Mat.zeros(destination.size(), CvType.CV_8UC3);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		// FILTER THE PHOTO
		// GRAYSCALE
		Imgproc.cvtColor(source, destination, Imgproc.COLOR_RGB2GRAY);
		// REMOVE PARKING LINES FROM IMAGE
		// PARKING MASK
		Core.subtract(destination, parkingMask, destination);
		// BINARY THE IMAGE INTO BLACK AND WHITE
		Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
		// THICKEN AND SMOOTH LINES
		Imgproc.dilate(destination, destination, new Mat(), new Point(-1, 1), 2);

		// FINDS CONTOURS
		Imgproc.findContours(destination, contours,new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(contourDest, contours, -1, new Scalar(255, 255, 255));
		// POPULATE CARS
		for(int i = 0; i < contours.size(); i++){
			Rect r = Imgproc.boundingRect(contours.get(i));
			if(r.area() >= 2000 && ((r.height / (double) r.width) > 1.0)) {
				cars.add(new ParkingSpot(r));
			}
		}
	}
	
	public void compareParkingLots(String source2) {
		// COMPARES THE ARRAYLIST OF emptyParkingLot RECTANGLES WITH THE ARRAYLIST OF THE NEW IMAGE RECTANGLES TO SEE IF THERE IS A SPOT TAKEN
		Mat result = Imgcodecs.imread("rsc/firstResult.png");
		int left, right, bottom, top;
		for(int i = 0; i < emptyParkingLot.size(); i++) {
			for(int j = 0; j < cars.size(); j++) {
				left = Math.max(emptyParkingLot.get(i).getX1(), cars.get(j).getX1());
				right = Math.min(emptyParkingLot.get(i).getX2(), cars.get(j).getX2());
				bottom = Math.min(emptyParkingLot.get(i).getY2(), cars.get(j).getY2());
				top = Math.max(emptyParkingLot.get(i).getY1(), cars.get(j).getY1());

				if (left < right && bottom > top) {
					if(spotsAvailable > 0) {
						spotsAvailable--;
					}
					Imgproc.rectangle(result, emptyParkingLot.get(i).r.tl(), emptyParkingLot.get(i).r.br(), new Scalar(0, 0, 255), -1);
					emptyParkingLot.get(i).available = false;
				}
			}
		}
		
		for(ParkingSpot p : emptyParkingLot) {
			if(p.available) {
				this.spotsAvailable++;
			}
		}
	    Imgcodecs.imwrite("rsc/result.png", result);
	}
	
	public void locateSpots(String before, String after) {
		cars.clear();
		emptyParkingLot.clear();
		num = 1;
		emptyParkingLotProcessing(connectPoints(before));
		activeParkingLotProcessing(after);
		compareParkingLots(before);
	}
	public void display(){
		for(ParkingSpot p : emptyParkingLot) {
			System.out.println(p.spotNumber);
		}
	}
}