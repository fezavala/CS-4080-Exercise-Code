
class WheeledVehicle vec where
  velocity :: vec -> Double
  distanceTraveled :: vec -> Double -> Double

data Bicycle = Bicycle
instance WheeledVehicle Bicycle where
  velocity Bicycle = 15.0
  distanceTraveled Bicycle t = t * velocity Bicycle
  
data Car = Car
instance WheeledVehicle Car where
  velocity Car = 80.0
  distanceTraveled car t = t * velocity Car

time :: Double
time = 5.0

my_bicycle_speed = distanceTraveled Bicycle time
my_car_speed = distanceTraveled Car time

main :: IO()
main = print (my_bicycle_speed, my_car_speed)