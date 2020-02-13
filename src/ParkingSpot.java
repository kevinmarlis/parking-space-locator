import org.opencv.core.Rect;

public class ParkingSpot {
	Rect r;
	int x1, y1;
	int x2, y2;
	int spotNumber;
	boolean available = true;
	
	public ParkingSpot(Rect r) {
		this.r = r;
		this.x1 = (int) r.tl().x;
		this.y1 = (int) r.tl().y;
		this.x2 = (int) r.br().x;
		this.y2 = (int) r.br().y;
	}
	
	public int getX1() {
		return this.x1;
	}
	public void setX1(int x) {
		this.x1 = x;
	}
	public int getY1() {
		return this.y1;
	}
	public void setY1(int y) {
		this.y1 = y;
	}
	public int getX2() {
		return this.x2;
	}
	public void setX2(int x) {
		this.x2 = x;
	}
	public int getY2() {
		return this.y2;
	}
	public void setY2(int y) {
		this.y2 = y;
	}
	public int getSpotNumber() {
		return spotNumber;
	}
	public void setSpotNumber(int n) {
		this.spotNumber = n;
	}
	public String getAvailable() {
		if (available == true)
			return "available";
		else
			return "taken";
	}
	public void setAvailable(boolean b) {
		this.available = b;
	}
}