public class CityNode
{
	public String name;
	public double lat, lon;
	public int pop;
	public int originalIndex;
    static final double kmToMile = 0.62;

	CityNode(String name, double lat, double lon, int pop) //regular constructor
	{
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.pop = pop;
	}
	
	CityNode(CityNode og) //deep copy constructor
	{
		this.name = og.name;
		this.lat = og.lat;
		this.lon = og.lon;
		this.pop = og.pop;
	}

	public static double getDistance(CityNode city1, CityNode city2)
	{
		var R = 6371; // Radius of the earth in km
		var dLat = deg2rad(city2.lat-city1.lat); // deg2rad below
		var dLon = deg2rad(city2.lon-city1.lon);
		var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(city1.lat)) * Math.cos(deg2rad(city2.lat)) * Math.sin(dLon/2) * Math.sin(dLon/2);
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		var d = R * c; // Distance in km
		//return d;
		return d * kmToMile;
	}
	
	public static double deg2rad(double deg)
	{
		return (deg * Math.PI/180);
	}
}
