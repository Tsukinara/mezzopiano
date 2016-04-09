public class Snow {
	public int x, y, dx, dy, dia;
	
	public Snow(double x, double y, double dx, double dy, double dia) {
		this.x = (int)x; this.y = (int)y; 
		this.dx = (int)dx; this.dy = (int)dy;
		this.dia = (int)dia;
	}
	
	public void step() {
		x += dx; y += dy;
		if (x > 1920) x = x-1920;
		if (y > 1080) y = y-1080;
	}
}