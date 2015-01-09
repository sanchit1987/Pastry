import akka.actor._


sealed trait PastryMessage

case object Simulation extends PastryMessage
case object Print extends PastryMessage
case object PrintTable extends PastryMessage
case class InsertNode(nodeId: String, neighbor : String, trace : Int) extends PastryMessage
case class Join(nodeId : String, trace : Int) extends PastryMessage
case class Welcome(nodeId : String, leaf : Array[String], routing : Array[Array[String]], trace: Int) extends PastryMessage
case class UpdateTables(nodeId : String, leaf : Array[String], routing : Array[Array[String]]) extends PastryMessage
case object NewNodeJoined extends PastryMessage
case object StartRouting extends PastryMessage
case class RouteMessage(key : String, hops : Int) extends PastryMessage
case class Listener(hops : Int) extends PastryMessage
case object KillNode extends PastryMessage

class Simulator(noOfNodes : Int, noOfMsges : Int, percentageOfFailure : Int) extends Actor {
  
    val rnd = new scala.util.Random
    Global.nodesCreated = noOfNodes-2
    var nodes = new Array[ActorRef](noOfNodes)
    var neighbor = ""
    val cols = 4
    val rows = Math.round(Math.log10(noOfNodes)/Math.log10(cols)).toInt + 1
    val length = rows
    var nodeValue = new Array[Int](noOfNodes)
    var last = noOfNodes-1
    var num=0
    var i = 0
    var hops = 0
    var nodeInBase4 = new Array[String](noOfNodes)
    var sum = 0
    var countReturnedNodes = 0
    var c = 0
    
    for(j<-0 until noOfNodes)
      nodeValue(j)=j
      
  
def receive = {
  
  case Simulation => {
    
    
          
          var trace=0
    	  var range = (0 to last)
    	  var x = range(rnd.nextInt(range length))
          num = nodeValue(x)	
          
          var nodeId = Integer.toString(num,4);
          var diff = Math.abs(length - nodeId.length)
      
        // println(diff)
        if(diff>0) nodeId = Global.prefixZeros(nodeId,diff)
        
        nodeInBase4(i) = nodeId
      
       println("(" + i + ")" + nodeId + "  up") 
      //println("new node up : " + nodeId)
      
      nodes(num) = context.actorOf(Props(new Nodes(nodeId,rows,cols)), nodeId)
      //println(nodes(num).path)
      if(i!=0) nodes(num) ! InsertNode(nodeId,neighbor,trace)
      else self ! Simulation
        
      
      neighbor = nodeId
      nodeValue(x) = nodeValue(last)
      last-=1
      
      i = i + 1
      
     // println("reached 1")
    
    
  }
  
  
//********************************************************************************************************************  
  
  case StartRouting =>{
    
  c+=1
  
  if(c==1){
 println("initiating routing of messages")  
 println("killing : " + percentageOfFailure + "%" + "of nodes")
   
 for(j<- 0 until ((percentageOfFailure*noOfNodes)/100).toInt){
   
 var range = (0 to noOfNodes-1)
 var x = range(rnd.nextInt(range length))

 
 println("killing node : " + nodes(x).path.name)
 nodes(j) ! KillNode
 Thread.sleep(50)
 } 

 }
 
// if(percentageOfFailure > 50) Thread.sleep(1000000)
 
 println("initiating routing of messages")  
 for(i<- 0 until noOfNodes){
   
 var range = (0 to noOfNodes-1)
 var x = range(rnd.nextInt(range length))
 var y = range(rnd.nextInt(range length))
 //println("source node :" + nodes(y).path.name + "  destination node : " + nodeInBase4(x))
 nodes(y) ! RouteMessage(nodeInBase4(x), hops)
 Thread.sleep(50)
 hops = 0
 }
    
    
  }
  
  
//************************************************************************************************************************
  

  
//******************************************************************************************************************** 
  
  case Listener(returnedHops)=>{
    
    sum+=  returnedHops
    countReturnedNodes+= 1
    
   // println("***************************************************************************************************")
    
    println("sum = " + sum)
    if(countReturnedNodes == noOfNodes){
    val avg: Double = sum/noOfNodes.toDouble
    println("Sum = " + sum + "  average no of hops taken = " + avg)
    System.exit(1)
    }
    
  }
  
  
//*********************************************************************************************************************
  case Print =>
    for(i<-0 until noOfNodes){
      nodes(i)! PrintTable
      Thread.sleep(20)
    }
  
}
  
  
}