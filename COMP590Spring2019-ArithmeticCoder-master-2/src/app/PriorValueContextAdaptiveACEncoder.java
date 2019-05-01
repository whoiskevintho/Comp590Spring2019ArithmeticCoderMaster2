package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class PriorValueContextAdaptiveACEncoder {
	
	public static void main(String[] args) throws IOException {
		String input_file_name = "data/out.dat";
		String output_file_name = "data/prior-value-test-compressed.dat";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
				
		Integer[] symbols = new Integer[256];
		for (int i=0; i<256; i++) {
			symbols[i] = i;
		}

		// Create 256 models. Model chosen depends on value of symbol prior to 
		// symbol being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];
		
		for (int i=0; i<256; i++) {
			// Create new model with default count of 1 for all symbols
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);

		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		
		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		//int sym = fis.read();
		//int sym = encoder.encode(next_symbol, model, bit_sink);
		
		//int count = 0;
				
		for (int i = 0; i < num_symbols; i++) {
			int sym = fis.read();
			if (i == 0) {
				System.out.println("loop one");
				encoder.encode(sym, model, bit_sink);
				}
			
			int[] priorindex = new int[4096];
			
			if (i >= 4096 * 256) {
				model = models[priorindex[i % 4096]];
			}
		
			//int sym = fis.read();
			
			encoder.encode(sym, model, bit_sink);
			
			// Update model used
			model.addToCount(sym);
			
			// Set up next model to use.
			priorindex[i % 4096] = sym;
			
  

				
			}
			
			System.out.println("Done.");
			//fos.flush();
			fis.close();
			encoder.emitMiddle(bit_sink);
			bit_sink.padToWord();
			fos.close();
			
			
	
		}
		
	}


