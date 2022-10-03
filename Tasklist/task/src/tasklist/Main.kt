package tasklist
import java.io.File
import kotlinx.datetime.*
import kotlin.math.ceil
import com.squareup.moshi.*

private operator fun <E> MutableList<E>.component6(): E = get(5)

const val savePath = "tasklist.json"
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
val taskListAdapter = moshi.adapter<MutableList<Task>>(type)

data class Task(val priority: String, val date: String, val time: String, val taskInfo: String) {

    private fun printBottom(){
        println("+----+------------+-------+---+---+--------------------------------------------+")
    }

    private fun formatTextLineArray(taskLine: List<String>): MutableList<String> {
        val finalTaskLine = mutableListOf<String>()
        for (line in taskLine) {
            val computedTotalLineNum = ceil(line.length / 44.0).toInt()
            for(m in 0 until computedTotalLineNum){
                try {
                    finalTaskLine.add(line.substring(m*44, m*44 + 44))
                } catch(e: StringIndexOutOfBoundsException){
                    finalTaskLine.add(line.substring(m*44, m*44 + (line.length % 44)))
                }
            }

        }
        return finalTaskLine
    }

    private fun renderPriority(): String{

        return when (priority.uppercase()){
            "C" -> "\u001B[101m \u001B[0m"
            "H" -> "\u001B[103m \u001B[0m"
            "N" -> "\u001B[102m \u001B[0m"
            else -> "\u001B[104m \u001B[0m"
        }
    }

    private fun renderDueTag(tag: String): String{

        return when (tag.uppercase()){
            "I" -> "\u001B[102m \u001B[0m"
            "T" -> "\u001B[103m \u001B[0m"
            else -> "\u001B[101m \u001B[0m"
        }
    }

    private fun printContent(lineArray: MutableList<String?>){
        if (lineArray.size < 6) return
        var (content, taskNum, date, time, priority, dueTag) = lineArray

        if(taskNum?.length == 1){
            taskNum = "$taskNum "
        }

        println("| ${taskNum  ?: "  "} |" +
                " ${date ?: " ".repeat(10) } |" +
                " ${time ?: " ".repeat(5) } |" +
                " ${if(priority != null) renderPriority() else " " } |" +
                " ${if(dueTag != null) renderDueTag(dueTag) else " " } |" +
                "${content + " ".repeat(44 - content!!.length) }|")
    }


    fun printTask(taskNum: Int){
        val taskLine = taskInfo.split("\n").filter { ele -> ele != "" }.toList()
        if(taskLine.isEmpty()) return
        val formatTaskLineList = formatTextLineArray(taskLine)

        printContent(mutableListOf(formatTaskLineList[0], taskNum.toString(), date, time, priority, genDueTag(date)))

        for(index in 1 until formatTaskLineList.size){
            printContent(mutableListOf(formatTaskLineList[index], null, null, null, null, null, null))
        }

        printBottom()
    }

    fun genDueTag (date: String): String {

        val dateList = date.split("-").map { ele -> ele.toInt() }
        val taskDate = LocalDate(dateList[0], dateList[1], dateList[2])
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)

        return when{
            numberOfDays == 0 -> "T"
            numberOfDays > 0 -> "I"
            else -> "O"
        }
    }
}

fun printHeader(){
    println("+----+------------+-------+---+---+--------------------------------------------+")
    println("| N  |    Date    | Time  | P | D |                   Task                     |")
    println("+----+------------+-------+---+---+--------------------------------------------+")
}

fun genTask(oldTask: Task? = null, newField: String? = null): Task? {

    var priority = ""
    var dateStr = oldTask?.date ?: ""
    var timeStr = ""
    val isNewTask = oldTask == null

    while(isNewTask || newField == "priority"){
        println("Input the task priority (C, H, N, L):")
        priority = readln()
        when(priority.uppercase()){
            "C", "H", "N", "L" -> break
            else -> println("")
        }
    }

    while (isNewTask || newField == "date") {
        println("Input the date (yyyy-mm-dd):")
        try {
            dateStr = readln()
            val l = dateStr.split("-").toMutableList()
            if(l[1].length == 1){
                l[1] = "0${l[1]}"
            }
            if(l[2].length == 1){
                l[2] = "0${l[2]}"
            }
            dateStr = l.joinToString(separator = "-").toLocalDate().toString()
            break
        }catch (e: RuntimeException){
            println("The input date is invalid")
        }
    }

    while (isNewTask || newField == "time") {
        println("Input the time (hh:mm):")
        try {
            timeStr = readln()
            val l = timeStr.split(":").toMutableList()
            if(l[0].length == 1){
                l[0] = "0${l[0]}"
            }
            if(l[1].length == 1){
                l[1] = "0${l[1]}"
            }
            timeStr = l.joinToString(separator = ":")
            Instant.parse("${dateStr}T$timeStr:00Z")
            break
        }catch (e: RuntimeException){
            println("The input time is invalid")
        }
    }

    var taskText = ""
    if(isNewTask  || newField == "task") println("Input a new task (enter a blank line to end):")
    while (isNewTask  || newField == "task") {
        val taskLn = readln().trim()
        if (taskLn == "") {
            if (taskText == "") {
                println("The task is blank")
            }
            break
        }
        taskText += taskLn + "\n"
    }

    return when (newField) {
        "priority" -> oldTask?.copy(priority=priority)
        "date" -> oldTask?.copy(date=dateStr)
        "time" -> oldTask?.copy(time=timeStr)
        "task" -> oldTask?.copy(taskInfo= taskText)
        else -> Task(
            priority=priority,
            date=dateStr,
            time=timeStr,
            taskInfo= taskText)
    }



}

fun addTask(taskList :MutableList<Task>) {
    val task = genTask()
    if( task != null ) taskList.add(task) else return
}


fun saveTask(taskListStr :String){
    File(savePath).writeText(taskListStr)
}

fun printTasks(taskList :MutableList<Task>) {

    if (taskList.size == 0) {
        println("No tasks have been input")
        return
    }

//    if (taskList.any { it.taskInfo.isEmpty() }) {
//        println("No tasks have been input")
//        return
//    }

    printHeader()
    for(i in taskList.indices){
        taskList[i].printTask(i+1)
    }
}
fun deleteTask(taskList :MutableList<Task>){
    val taskLength = taskList.size

    if( taskLength == 0 ){
        return println("No tasks have been input")
    }

    printTasks(taskList)

    while (true) {
        println("Input the task number (1-$taskLength):")
        val taskNum = try {
            readln().toInt()
        } catch (e: Exception) {
            println("Invalid task number")
            continue
        }

        if (taskNum !in 1..taskLength) {
            println("Invalid task number")
            continue
        }

        taskList.removeAt(taskNum-1)
        println("The task is deleted")
        break
    }
}

fun editTask(taskList :MutableList<Task>) {
    val taskLength = taskList.size

    if (taskLength == 0) {
        return println("No tasks have been input")
    }

    printTasks(taskList)

    while (true) {
        println("Input the task number (1-$taskLength):")
        val taskNum = try {
            readln().toInt()
        } catch (e: Exception) {
            println("Invalid task number")
            continue
        }

        if (taskNum !in 1..taskLength) {
            println("Invalid task number")
            continue
        }

        while (true) {
            println("Input a field to edit (priority, date, time, task):")
            when (val newField = readln()) {
                "priority", "date", "time", "task" -> {
                    val newTask = genTask(taskList[taskNum - 1], newField)
                    if (newTask != null) taskList[taskNum - 1] = newTask
                    println("The task is changed")
                    return
                }

                else -> println("Invalid field")
            }
        }
    }
}

fun fetchTaskList (): MutableList<Task>? {
    val f = File(savePath)

    if (!f.exists()) {
        return null
    }

    if (f.length() == 0L) {
        return null
    }

    val json = f.readText().trimIndent()
    return taskListAdapter.fromJson(json)
}

fun main() {

    val taskList = fetchTaskList() ?: mutableListOf<Task>()

    loop@while(true){
        println("Input an action (add, print, edit, delete, end):")
        val ans = readln()

        when (ans) {
            "add" -> addTask(taskList)
            "print" -> printTasks(taskList)
            "edit" -> editTask(taskList)
            "delete" -> deleteTask(taskList)
            "end" -> {
                saveTask(taskListAdapter.toJson(taskList) ?: "")
                println("Tasklist exiting!")
                break
            }
            else -> println("The input action is invalid")
        }
    }
}

