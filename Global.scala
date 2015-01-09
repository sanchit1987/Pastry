object Global {
  
  var trace = 0;
  var nodesCreated = 0;

def prefixZeros(num : String, numOfZeros : Int):String = {
  
 
  var number = num
  
  
  for(i <- 0 until numOfZeros)
    number = "0" + number

    return number
}

def ConvertToDecimal(x : Int): Int={
 
var sum:Int = 0
var i:Int  = 0
var rem:Int = 0
var num:Int = x

while(num!=0)
{   
rem = num%10
sum = sum + Math.pow(4,i).toInt*rem
num = num/10
i = i+1
}

return sum
}



}