import akka.actor._
import collection.mutable.ListBuffer
import Array._
import scala.util.control.Breaks._

class Nodes(nodeId: String, rows: Int, cols: Int) extends Actor {

  var routingTable = Array.ofDim[String](rows, cols)
  var leafSet = ofDim[String](cols)
  val helpers = ListBuffer[ActorRef]()
  var counter = 0


  //**********************************************************************************************************************	

  def printTable(leaf: Array[String], routing: Array[Array[String]]) = {

    println("leafset table = \n ")

    for(i<- 0 until cols) print(leaf(i)+"\t")

    println("\n\n routing table = \n ")

    for (i <- 0 until rows) {
      for(j<- 0 until cols){
        
        print(routing(i)(j)+"\t")
        
      }
      println("\n")
    }

  }

  //**********************************************************************************************************************

  def getMatch(x: String, y: String): Int = {

    var count = 0

    for (i <- 0 until x.length) {
      
      if(x(i) != y(i)) return count
      if (x(i) == y(i)) count += 1;

    }

    return count;

  }

  //**********************************************************************************************************************

  def InsertInLeaf(node: String): Int = {

    
    var x = Global.ConvertToDecimal(nodeId.toInt)
    var y = Global.ConvertToDecimal(node.toInt)
    
    //println(nodeId + " - " + node + " = " + Math.abs(x-y))
    
    
    var diffWithIncoming = Math.abs( x - y )
    
   

    if (diffWithIncoming <= 2)
      for (i <- 0 until cols) {

        if(leafSet(i) == node) return -1
        
        
        if (leafSet(i) == null) {

          leafSet(i) = node
          return 1

        }
      }

    return 0
  }

  //**********************************************************************************************************************

  def InsertInRouting(node: String): Boolean = {

    

    val diffWithIncoming = Math.abs(nodeId.toInt - node.toInt)


    var common = getMatch(nodeId, node)

    
    
    
      val i = node(common).toString.toInt
      
      if(routingTable(common)(i) == node) return false
      if (routingTable(common)(i) == null) {

        routingTable(common)(i) = node
        return true

      }

    

    

      var diff = Math.abs(nodeId.toInt - routingTable(common)(i).toInt)

      if (diff < diffWithIncoming) {
       routingTable(common)(i) = node
       return true

      }

    



    return false

  }

  //*************************************************************************************************************************8

  def receive = {

    case InsertNode(node, neighbor, trace) => {

      //println("here")
     // println(" request to insert node : " + node)
      val neighborRef = context.actorSelection("/user/simulator/" +neighbor)
      //println("sending join request to neighbor : " + neighbor)
      neighborRef ! Join(node, trace)
     

    }

    //******************************************************************************************************************   
    case Join(key, trace) => {

      // println("reached node : " + self.path.name)
      val keyVal = key.toInt
      val nodeVal = nodeId.toInt
      val diffWithPresent = Math.abs(nodeVal - keyVal)
      var diffWithRest = 0
      var least = diffWithPresent
      var leastInString = nodeId

      Global.trace = Global.trace + 1

      for (i <- 0 until leafSet.length) {
        if (leafSet(i) != null) {
          diffWithRest = Math.abs(keyVal - leafSet(i).toInt)
          if (diffWithRest < least) {
            least = diffWithRest
            leastInString = leafSet(i)
          }
        }

      }
      
      for (i <- 0 until rows) {
        for (j <- 0 until cols) {
          if (routingTable(i)(j) != null) {
            diffWithRest = Math.abs(keyVal - routingTable(i)(j).toInt)

            if (diffWithRest < least) {
              least = diffWithRest
              leastInString = routingTable(i)(j)
            }
          }
        }

      }

      if (least != diffWithPresent) {
        
        leastInString = "/user/simulator/" + leastInString 
        val next = context.actorSelection(leastInString)
        //println("address of node : " + leastInString + " : " + next)
       //println("node : " + self.path.name + "forwarding request to node : " + leastInString)
        next ! Join(key, Global.trace)

      }
       
      Thread.sleep(50)
      val newNode = context.actorSelection("/user/simulator/" + key)
      //println("node : " + self.path.name + "  sending welcome to node : " + key)
      newNode ! Welcome(nodeId, leafSet, routingTable, Global.trace)

    }
    //************************************************************************************************************************

    case Welcome(node, leaf, routing, trace) => {

      helpers += sender
      counter+=1
      
     //println("received welcome from node : " + node + "  by node : " + self.path.name)
      
      
      if (InsertInLeaf(node)==0) InsertInRouting(node)

      for (id <- leaf)
        if (id != null && id != nodeId)
          if (InsertInLeaf(id)==0) InsertInRouting(id)

      for (i <- 0 until rows) {

        for (j <- 0 until cols) {

          if (routing(i)(j) != null && routing(i)(j) != nodeId)
           if (InsertInLeaf(routing(i)(j))==0) InsertInRouting(routing(i)(j))

        }
      }

     

      if (counter == Global.trace) {
        for (elt <- helpers){
          //println("new node : " + self.path.name + " sending updated tables to node : " + elt.path.name )
          elt ! UpdateTables(nodeId, leafSet, routingTable)
          
        }
        counter = 0
      }

      

    }

    //**************************************************************************************************************************    
    case UpdateTables(node, leaf, routing) => {

      // trying to insert incoming node's Id either in leafset or routing table
      
     // println("node : " + self.path.name + "  recevied updates from new node : " + node )
       
      if (InsertInLeaf(node)==0) InsertInRouting(node)

      for (id <- leaf)
        if (id != null && id != nodeId)
          if (InsertInLeaf(id)==0) InsertInRouting(id)

      for (i <- 0 until rows) {

        for (j <- 0 until cols) {

          if (routing(i)(j) != null && routing(i)(j) != nodeId)
           if (InsertInLeaf(routing(i)(j))==0) InsertInRouting(routing(i)(j))

        }
      }

      sender ! NewNodeJoined

    }

//**********************************************************************************************************************

    case NewNodeJoined => {

      Global.trace -= 1
      // println("nodes created " + Global.nodesCreated ) 
      
      
      if (Global.nodesCreated != 0) {
        if (Global.trace == 0) {
          Global.nodesCreated -= 1
           //printTable(leafSet, routingTable)
         //println("**********************************************************************************")
         Thread.sleep(50)
          context.parent ! Simulation
        }
      } else {
    	 
        //println("here here")
    	//context.parent ! Print
        context.parent ! StartRouting

      }

    }
    
//******************************************************************************************************************
    
    case RouteMessage(key, hops)=>{
      
     // println("reached node : " + nodeId)
      //println("finding key : " + key)
      val keyVal = key.toInt
      val nodeVal = nodeId.toInt
      val diffWithPresent = Math.abs(nodeVal - keyVal)
      var diffWithRest = 0
      var least = diffWithPresent
      var leastInString = nodeId
      var hop = hops
     
      //print(nodeId + "-->")
      
      if(nodeId == key) context.parent ! Listener(hop)
      else {
      for (i <- 0 until leafSet.length) {
        if (leafSet(i) != null) {
          diffWithRest = Math.abs(keyVal - leafSet(i).toInt)
          if (diffWithRest < least) {
            least = diffWithRest
            leastInString = leafSet(i)
          }
        }

      }
      
      for (i <- 0 until rows) {
        for (j <- 0 until cols) {
          if (routingTable(i)(j) != null) {
            diffWithRest = Math.abs(keyVal - routingTable(i)(j).toInt)

            if (diffWithRest < least) {
              least = diffWithRest
              leastInString = routingTable(i)(j)
            }
          }
        }

      }

      
      
      
      if (least != diffWithPresent) {
        
         hop+=1
        leastInString = "/user/simulator/" + leastInString 
        val next = context.actorSelection(leastInString)
        //println("address of node : " + leastInString + " : " + next)
         // println("node : " + self.path.name + "forwarding request to node : " + leastInString)
        next ! RouteMessage(key, hop)

      }
     
      }
      
    }
    
//**********************************************************************************************************************    
    case KillNode =>{
    
    context.stop(self)
    
    
  }

    //***********************************************************************************************************************    
    case PrintTable =>
     println("\nTables of :" + self.path.name)
      printTable(leafSet, routingTable)
  }

}