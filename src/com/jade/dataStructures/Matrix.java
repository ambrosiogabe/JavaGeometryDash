package com.jade.dataStructures;

public class Matrix {
    public int numRows, numColumns;
    public float data[][];

    public Matrix(int rows, int cols) {
        this.numRows = rows;
        this.numColumns = cols;
        data = new float[this.numRows][this.numColumns];
        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                this.data[i][j] = 0.0f;
            }
        }
    }

    public void randomize() {
        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                this.data[i][j] = (float)(Math.random() * 2.0 - 1);
            }
        }
    }

    public Matrix copy() {
        Matrix output = new Matrix(this.numRows, this.numColumns);
        for (int i=0; i < output.data.length; i++) {
            for (int j=0; j < output.data[0].length; j++) {
                output.data[i][j] = this.data[i][j];
            }
        }

        return output;
    }

    public static Matrix transpose(Matrix m) {
        Matrix res = new Matrix(m.numColumns, m.numRows);

        for (int i=0; i < m.numRows; i++) {
            for (int j=0; j < m.numColumns; j++) {
                res.data[j][i] = m.data[i][j];
            }
        }

        return res;
    }

    public static Matrix multiply(Matrix a, Matrix b) throws Exception {
        if (a.numColumns != b.numRows) throw new Exception("Matrix.multiply(a, b) expects a.numColumns == b.numRows");

        Matrix res = new Matrix(a.numRows, b.numColumns);
        for (int i=0; i < a.numRows; i++) {
            for (int j=0; j < b.numColumns; j++) {
                float tmp = 0;
                for (int k=0; k < a.numColumns; k++) {
                    tmp += (a.data[i][k] * b.data[k][j]);
                }
                res.data[i][j] = tmp;
            }
        }

        return res;
    }

    public void multiply(int n) {
        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                this.data[i][j] *= n;
            }
        }
    }

    public void multiply(float n) {
        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                this.data[i][j] *= n;
            }
        }
    }

    public void add(Matrix n) throws Exception {
        if (n.numColumns != this.numColumns || n.numRows != this.numRows) throw new Exception("Matrix.add(n) must use to matrixes of the same depth and width");

        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                this.data[i][j] += n.data[i][j];
            }
        }
    }

    public float[][] toArray() {
        float[][] res = new float[this.numColumns][this.numRows];
        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                res[i][j] = this.data[i][j];
            }
        }

        return res;
    }

    public static Matrix toMatrix(float[][] input) {
        Matrix m = new Matrix(input.length, input[0].length);
        for (int i=0; i < input.length; i++) {
            for (int j=0; j < input[0].length; j++) {
                m.data[i][j] = input[i][j];
            }
        }
        return m;
    }

    public void print() {
        System.out.println("Matrix");
        for (int i=0; i < this.numRows; i++) {
            for (int j=0; j < this.numColumns; j++) {
                System.out.printf("%5.2f ", this.data[i][j]);
            }
            System.out.println();
        }
    }
}
