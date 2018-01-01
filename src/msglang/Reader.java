package msglang;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;

import org.antlr.v4.runtime.Lexer;
import msglang.parser.*;
import msglang.AST.*;

public class Reader {
	
	public Program read() throws IOException {
		String programText = readNextProgram(getProgramDirectory());
		return parse(programText);
	}
	
	public Program parse(String programText) {
		Lexer l = getLexer(new org.antlr.v4.runtime.ANTLRInputStream(programText));
		MsgLangParser p = getParser(new org.antlr.v4.runtime.CommonTokenStream(l));
		Program program = p.program().ast;
		return program;
	}
	
	protected Lexer getLexer(org.antlr.v4.runtime.ANTLRInputStream s) {
		return new MsgLangLexer(s);
	}
	
	protected MsgLangParser getParser(org.antlr.v4.runtime.CommonTokenStream s) {
		return new MsgLangParser(s);
	}
			
	protected String getProgramDirectory() { return "build"+File.separator+"msglang"+File.separator+"examples"+File.separator; }
	
	public static String readFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(
				new FileReader(fileName))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
		}
	}
	
	public static String runFile(String programText, String programDirectory) throws IOException {
		if(programText.startsWith("run ")){
			programText = readFile(programDirectory + programText.substring(4));
		}
		return programText; 
	}	
	
	public static String readNextProgram(String programPath) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("$ ");
		String programText = br.readLine();
		return runFile(programText, programPath);
	}

}