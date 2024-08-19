package p1;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CabCounter {
    public static void main(String[] args) throws Exception {
        // setup stream execution env
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // checking input parameters
        final ParameterTool params = ParameterTool.fromArgs(args);

        // make parameters available in web interface
        env.getConfig().setGlobalJobParameters(params);


        DataStream<String> row = env.readTextFile("file:///home/vhugo/cab+flink.txt");

        // map string row to a tuple
        DataStream<CabTrip> trip = row.map(new MapFunction<String, CabTrip>() {
            public CabTrip map(String value){
                String[] fields = value.split(",");
                Boolean isOnGoing = fields[4].equals("yes") ? true : false;

                Integer passengerCount;
                try {
                    passengerCount = Integer.parseInt(fields[7]);
                } catch (NumberFormatException e) {
                    passengerCount = 0;
                }

                return new CabTrip(
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    isOnGoing,
                    fields[5],
                    fields[6],
                    passengerCount
                );
            } 
        });

        DataStream<Tuple2<String, Integer>> destionationPassangerCount = trip.map(new MapFunction<CabTrip, Tuple2<String, Integer>>() {
            public Tuple2<String, Integer> map(CabTrip value){
                return new Tuple2<String, Integer>(value.getDestination(), value.getPassengerCount());
            }
            
        });

        DataStream<Tuple2<String, Integer>> popularDestination = destionationPassangerCount.keyBy(0).sum(1).keyBy(value -> 0).maxBy(1);

        // Average number of passengers from each pickup location.  
        // | average =  total no. of passengers from a location / no. of trips from that location.
        
        // FromLocation, Passengers, TripNumber
        DataStream<Tuple3<String, Integer, Integer>> tripsFromLocation = trip
            .map(new MapFunction<CabTrip, Tuple3<String, Integer, Integer>>() {
                public Tuple3<String, Integer, Integer> map(CabTrip value) {
                    return new Tuple3<String, Integer, Integer>(value.getPickupLocation(), value.getPassengerCount(), 1);
                }
            }).keyBy(0).reduce(
                new ReduceFunction<Tuple3<String,Integer,Integer>>() {
                    public Tuple3<String,Integer,Integer> reduce(Tuple3<String,Integer,Integer> trip1, Tuple3<String,Integer,Integer> trip2){
                        return new Tuple3<>(trip1.f0, trip1.f1 + trip2.f1, trip1.f2 + trip2.f2);
                    }
            });
        

        DataStream<Tuple2<String, Float>> avgPassengersFromLocation = tripsFromLocation.map(
            new MapFunction<Tuple3<String,Integer,Integer>, Tuple2<String, Float>>() {
                public Tuple2<String, Float> map(Tuple3<String, Integer, Integer> value) {
                    return new Tuple2<String, Float>(value.f0, value.f1.floatValue() / value.f2.floatValue());
                }
            });


        // Avg Number of trips for each driver | no. o passangers drivers has picked / total no. of trips he has made
        
        DataStream<Tuple3<String, Integer, Integer>> driverTrips = trip.map(new MapFunction<CabTrip, Tuple3<String, Integer, Integer>>() {
            public Tuple3<String, Integer, Integer> map(CabTrip value) {
                return new Tuple3<String, Integer, Integer>(value.getDriverName(), value.getPassengerCount(), 1);
            }
        }).keyBy(0).reduce(
            new ReduceFunction<Tuple3<String,Integer,Integer>>() {
                public Tuple3<String, Integer, Integer> reduce(Tuple3<String, Integer, Integer> value1, Tuple3<String, Integer, Integer> value2){ 
                    return new Tuple3<>(value1.f0, value1.f1 + value2.f1, value1.f2 + value2.f2);
                }
            }
        );

        DataStream<Tuple2<String, Float>> avgTripsPerDriver = driverTrips.map(new MapFunction<Tuple3<String,Integer,Integer>, Tuple2<String, Float>>() {
            public Tuple2<String, Float> map(Tuple3<String, Integer, Integer> value) {
                return new Tuple2<String, Float>(value.f0, value.f1.floatValue() / value.f2.floatValue());
            }
        });
        
        avgTripsPerDriver.writeAsCsv("file:///home/vhugo/avgPerDriver", FileSystem.WriteMode.OVERWRITE);
        avgPassengersFromLocation.writeAsCsv("file:///home/vhugo/avgPassengersFromLocation", FileSystem.WriteMode.OVERWRITE);
        popularDestination.writeAsCsv("file:///home/vhugo/popularDestination", FileSystem.WriteMode.OVERWRITE);

        env.execute("Popular Destination");
    }
}
