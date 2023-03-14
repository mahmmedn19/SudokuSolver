package com.example.sudoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private val numRows = 9
    private val numCols = 9
    private val cellSize = 100
    private lateinit var buttonsLayout: LinearLayout
    private lateinit var btnSolve: Button
    private lateinit var btnClear: Button
    private lateinit var btnClearAll: Button
    private var selectedCell: TextView? = null // keep track of the currently selected cell
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gridLayout = findViewById(R.id.grid_layout)
        buttonsLayout = findViewById(R.id.buttons_layout)
        btnSolve = findViewById(R.id.btn_solve)
        btnClear = findViewById(R.id.btn_clear)
        btnClearAll = findViewById(R.id.btn_clear_all)
        gridLayout.columnCount = numCols
        gridLayout.rowCount = numRows
        // Set listeners for number buttons
        for (i in 1..9) {
            val buttonId = resources.getIdentifier("num${i}Button", "id", packageName)
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener { onNumberButtonClick(i) }
        }
        // Initialize empty Sudoku grid
        val grid = Array(numRows) { IntArray(numCols) }

        createCells(grid)

        // Set listeners for solve, clear, and clear all buttons
        btnSolve.setOnClickListener {
            val grid = getCurrentGridState()
            // Check if at least 10 cells are filled
            var numFilledCells = 0
            for (row in 0 until numRows) {
                for (col in 0 until numCols) {
                    if (grid[row][col] != 0) {
                        numFilledCells++
                    }
                }
            }
            if (numFilledCells < 17) {
                Toast.makeText(this, "Please fill in at least 17 cells before solving", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (getSudokuSolution(grid)) {
                updateGrid(grid)
            } else {
                Toast.makeText(this, "Unable to solve Sudoku", Toast.LENGTH_SHORT).show()
            }
        }
        btnClear.setOnClickListener { clearSelectedCell() }
        btnClearAll.setOnClickListener { clearAllCells() }
    }
    private fun getCurrentGridState(): Array<IntArray> {
        val gridState = Array(numRows) { IntArray(numCols) }

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val cell = gridLayout.getChildAt(row * numCols + col) as TextView
                val cellValue = cell.text.toString().toIntOrNull() ?: 0
                gridState[row][col] = cellValue
            }
        }

        return gridState
    }
    private fun getSudokuSolution(grid: Array<IntArray>): Boolean {
        // Find the first empty cell
        var row = -1
        var col = -1
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                if (grid[r][c] == 0) {
                    row = r
                    col = c
                    break
                }
            }
            if (row != -1) {
                break
            }
        }

        // If there are no empty cells, the grid is already solved
        if (row == -1) {
            return true
        }

        // Try all possible values for the current cell
        for (num in 1..9) {
            if (isValidMove(grid, row, col, num)) {
                grid[row][col] = num
                if (getSudokuSolution(grid)) {
                    return true
                }
                grid[row][col] = 0
            }
        }

        // If no value works for the current cell, backtrack
        return false
    }

    private fun isValidMove(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row and column for conflicts
        for (i in 0 until numCols) {
            if (grid[row][i] == num || grid[i][col] == num) {
                return false
            }
        }

        // Check 3x3 box for conflicts
        val boxRow = row - row % 3
        val boxCol = col - col % 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (grid[r][c] == num) {
                    return false
                }
            }
        }

        // If no conflicts found, the move is valid
        return true
    }
    private fun createCells(grid: Array<IntArray>) {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val cell = createCell(row, col, grid)
                gridLayout.addView(cell)
            }
        }
    }

    private fun createCell(row: Int, col: Int, grid: Array<IntArray>): TextView {
        val cell = TextView(this)
        val params = GridLayout.LayoutParams()
        params.width = cellSize
        params.height = cellSize
       // params.setMargins(2, 2, 2, 2)
        if ((row % 9 == 2 || row % 9 == 5) && (col % 9 == 2 || col % 9 == 5)) {
            params.setMargins(2, 2, 16, 16)
        } else if (row % 9 == 2 || row % 9 == 5) {
            params.setMargins(2, 2, 2, 16)
        } else if (col % 9 == 2 || col % 9 == 5) {
            params.setMargins(2, 2, 16, 2)
        } else {
            params.setMargins(2, 2, 2, 2)
        }
        params.columnSpec = GridLayout.spec(col, 1f)
        params.rowSpec = GridLayout.spec(row, 1f)
        cell.layoutParams = params
        cell.gravity = Gravity.CENTER
        cell.setTextColor(ContextCompat.getColor(this, R.color.black))
        cell.setBackgroundResource(R.drawable.cell_border)
        val value = grid[row][col]
        if (value != 0) {
            cell.text = value.toString()
            cell.isClickable = false
            cell.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200))
        } else {
            cell.setOnClickListener {
                // Toggle the cell background and update its text on click
                // Clear the selection of the previously selected cell (if any)
                selectedCell?.isSelected = false
                selectedCell?.setBackgroundResource(R.drawable.cell_border)

                // Set the selected cell and change its background color
                selectedCell = cell
                cell.isSelected = true
                cell.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
            }
        }
        return cell
    }

    private fun updateGrid(grid: Array<IntArray>) {
        gridLayout.removeAllViews()
        createCells(grid)
    }
    private fun onNumberButtonClick(num: Int) {
        // Set the text of the selected cell to the input value (if a cell is selected)
        selectedCell?.let {
            it.text = num.toString()

            // Clear the selection of the previously selected cell
            it.isSelected = false
            it.setBackgroundResource(R.drawable.cell_border)

            // Clear the reference to the selected cell
            selectedCell = null
        }
    }

    private fun getSelectedCell(): TextView? {
        // Iterate through all cells in the grid layout and return the first selected cell
        for (i in 0 until gridLayout.childCount) {
            val cell = gridLayout.getChildAt(i) as TextView
            if (cell.isSelected) {
                return cell
            }
        }
        return null
    }

    private fun clearSelectedCell() {
        // Get the currently selected cell and clear its text and background color
        selectedCell = getSelectedCell()
        selectedCell?.let {
            it.text = ""
            it.isSelected = false
            it.setBackgroundResource(R.drawable.cell_border)
        }
    }

    private fun clearAllCells() {
        // Iterate through all cells in the grid layout and clear their text and background color
        for (i in 0 until gridLayout.childCount) {
            val cell = gridLayout.getChildAt(i) as TextView
            cell.text = ""
            cell.isSelected = false
            cell.setBackgroundResource(R.drawable.cell_border)
        }
    }

}
