import akka.actor._
import com.typesafe.config.ConfigFactory

object Start {
  
  def main(args: Array[String])={
    
   val noOfNodes = Integer.parseInt(args(0))
   val noOfMsges = Integer.parseInt(args(1))
   val percentage = Integer.parseInt(args(2))
   val config = ConfigFactory.parseString("akka.log-dead-letters=off").withFallback(ConfigFactory.load())
   val system = ActorSystem.apply("project3",config)
   val initiate = system.actorOf(Props (new Simulator(noOfNodes, noOfMsges,percentage)),"simulator")
   initiate ! Simulation
    
  }

}