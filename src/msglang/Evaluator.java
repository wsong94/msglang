package msglang;
import java.util.List;

import msglang.AST.ProcExp;
import msglang.AST.SelfExp;
import msglang.AST.SendExp;
import msglang.AST.StopExp;
import msglang.Env.*;

import java.util.ArrayList;

import static msglang.AST.*;
import static msglang.Heap.*;
import static msglang.Value.*;

import java.io.File;
import java.io.IOException;

public class Evaluator implements Visitor<Value> {
	
	Printer.Formatter ts = new Printer.Formatter();

	Env initEnv = initialEnv(); //New for definelang    
    
    Value valueOf(Program p) {
    	Heap heap = new Heap16Bit();
		return (Value) p.accept(this, initEnv, heap);
	}
	
	@Override
	public Value visit(AddExp e, Env env, Heap h) {
		List<Exp> operands = e.all();
		double result = 0;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env, h); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new NumVal(result);
	}
	
	@Override
	public Value visit(UnitExp e, Env env, Heap h) {
		return new UnitVal();
	}

	@Override
	public Value visit(NumExp e, Env env, Heap h) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(StrExp e, Env env, Heap h) {
		return new StringVal(e.v());
	}

	@Override
	public Value visit(BoolExp e, Env env, Heap h) {
		return new BoolVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env, Heap h) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env, h);
		double result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env, h);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(ErrorExp e, Env env, Heap h) {
		return new Value.DynamicError("Encountered an error expression");
	}

	@Override
	public Value visit(MultExp e, Env env, Heap h) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env, h); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env env, Heap h) {
		for(DefineDecl d: p.decls())
			d.accept(this, initEnv, h);
		return (Value) p.e().accept(this, initEnv, h);
	}

	@Override
	public Value visit(SubExp e, Env env, Heap h) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env, h);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env, h);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env, Heap h) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}	

	@Override
	public Value visit(LetExp e, Env env, Heap h) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());
		
		for(Exp exp : value_exps){
			Value value = (Value)exp.accept(this, env, h);
			if(! (value instanceof DynamicError))
				values.add(value);
			else 
				return value;
		}
		
		Env new_env = env;
		for (int index = 0; index < names.size(); index++)
			new_env = new ExtendEnv(new_env, names.get(index), values.get(index));

		return (Value) e.body().accept(this, new_env, h);		
	}	
	
	@Override
	public Value visit(DefineDecl e, Env env, Heap h) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env, h);
		initEnv = new ExtendEnv(initEnv, name, value);
		return new Value.UnitVal();		
	}	

	@Override
	public Value visit(LambdaExp e, Env env, Heap h) { // New for funclang.
		return new Value.FunVal(env, e.formals(), e.body());
	}
	
	@Override
	public Value visit(CallExp e, Env env, Heap h) { // New for funclang.
		Object result = e.operator().accept(this, env, h);
		if(!(result instanceof Value.FunVal))
			return new Value.DynamicError("Operator not a function in call " +  ts.visit(e, env, h));
		Value.FunVal operator =  (Value.FunVal) result; //Dynamic checking
		List<Exp> operands = e.operands();

		// Call-by-value semantics
		List<Value> actuals = new ArrayList<Value>(operands.size());
		for(Exp exp : operands) 
			actuals.add((Value)exp.accept(this, env, h));
		
		List<String> formals = operator.formals();
		if (formals.size()!=actuals.size())
			return new Value.DynamicError("Argument mismatch in call " + ts.visit(e, env, h));

		Env closure_env = operator.env();
		Env fun_env = appendEnv(closure_env, initEnv);
		for (int index = 0; index < formals.size(); index++)
			fun_env = new ExtendEnv(fun_env, formals.get(index), actuals.get(index));
		
		return (Value) operator.body().accept(this, fun_env, h);
	}
	
	/* Helper for CallExp */
	/***
	 * Create an env that has bindings from fst appended to bindings from snd.
	 * The order of bindings is bindings from fst followed by that from snd.
	 * @param fst
	 * @param snd
	 * @return
	 */
	private Env appendEnv(Env fst, Env snd){
		if(fst.isEmpty()) return snd;
		if(fst instanceof ExtendEnv) {
			ExtendEnv f = (ExtendEnv) fst;
			return new ExtendEnv(appendEnv(f.saved_env(),snd), f.var(), f.val());
		}
		ExtendEnvRec f = (ExtendEnvRec) fst;
		return new ExtendEnvRec(appendEnv(f.saved_env(),snd), f.names(), f.vals());
	}
	/* End: helper for CallExp */
	
	@Override
	public Value visit(IfExp e, Env env, Heap h) { // New for funclang.
		Object result = e.conditional().accept(this, env, h);
		if(!(result instanceof Value.BoolVal))
			return new Value.DynamicError("Condition not a boolean in expression " +  ts.visit(e, env, h));
		Value.BoolVal condition =  (Value.BoolVal) result; //Dynamic checking
		
		if(condition.v())
			return (Value) e.then_exp().accept(this, env, h);
		else return (Value) e.else_exp().accept(this, env, h);
	}

	@Override
	public Value visit(LessExp e, Env env, Heap h) { // New for funclang.
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env, h);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env, h);
		return new Value.BoolVal(first.v() < second.v());
	}
	
	@Override
	public Value visit(EqualExp e, Env env, Heap h) { // New for funclang.
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env, h);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env, h);
		return new Value.BoolVal(first.v() == second.v());
	}

	@Override
	public Value visit(GreaterExp e, Env env, Heap h) { // New for funclang.
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env, h);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env, h);
		return new Value.BoolVal(first.v() > second.v());
	}
	
	@Override
	public Value visit(CarExp e, Env env, Heap h) { 
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env, h);
		return pair.fst();
	}
	
	@Override
	public Value visit(CdrExp e, Env env, Heap h) { 
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env, h);
		return pair.snd();
	}
	
	@Override
	public Value visit(ConsExp e, Env env, Heap h) { 
		Value first = (Value) e.fst().accept(this, env, h);
		Value second = (Value) e.snd().accept(this, env, h);
		return new Value.PairVal(first, second);
	}

	@Override
	public Value visit(ListExp e, Env env, Heap h) { 
		List<Exp> elemExps = e.elems();
		int length = elemExps.size();
		if(length == 0)
			return new Value.Null();
		
		//Order of evaluation: left to right e.g. (list (+ 3 4) (+ 5 4)) 
		Value[] elems = new Value[length];
		for(int i=0; i<length; i++)
			elems[i] = (Value) elemExps.get(i).accept(this, env, h);
		
		Value result = new Value.Null();
		for(int i=length-1; i>=0; i--) 
			result = new PairVal(elems[i], result);
		return result;
	}
	
	@Override
	public Value visit(NullExp e, Env env, Heap h) {
		Value val = (Value) e.arg().accept(this, env, h);
		return new BoolVal(val instanceof Value.Null);
	}
	
	@Override
	public Value visit(IsListExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.PairVal &&
				((Value.PairVal) val).isList() ||
				val instanceof Value.Null);
	}

	@Override
	public Value visit(IsPairExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.PairVal);
	}

	@Override
	public Value visit(IsUnitExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.UnitVal);
	}

	@Override
	public Value visit(IsProcedureExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.FunVal);
	}

	@Override
	public Value visit(IsStringExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.StringVal);
	}

	@Override
	public Value visit(IsNumberExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.NumVal);
	}

	@Override
	public Value visit(IsBooleanExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.BoolVal);
	}

	@Override
	public Value visit(IsNullExp e, Env env, Heap h) {
		Value val = (Value) e.exp().accept(this, env, h);
		return new BoolVal(val instanceof Value.Null);
	}

	public Value visit(EvalExp e, Env env, Heap h) {
		StringVal programText = (StringVal) e.code().accept(this, env, h);
		Program p = _reader.parse(programText.v());
		return (Value) p.accept(this, env, h);
	}

	public Value visit(ReadExp e, Env env, Heap h) {
		StringVal fileName = (StringVal) e.file().accept(this, env, h);
		try {
			String text = Reader.readFile("" + System.getProperty("user.dir") + File.separator + fileName.v());
			return new StringVal(text);
		} catch (IOException ex) {
			return new Value.DynamicError(ex.getMessage());
		}
	}
	
	@Override
	public Value visit(LetrecExp e, Env env, Heap h) { // New for reclang.
		List<String> names = e.names();
		List<Exp> fun_exps = e.fun_exps();
		List<Value.FunVal> funs = new ArrayList<Value.FunVal>(fun_exps.size());
		
		for(Exp exp : fun_exps) 
			funs.add((Value.FunVal)exp.accept(this, env, h));

		Env new_env = new ExtendEnvRec(env, names, funs);
		return (Value) e.body().accept(this, new_env, h);		
	}	
    
	@Override
	public Value visit(RefExp e, Env env, Heap h) { // New for reflang.
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env, h);
		return h.ref(value);
	}

	@Override
	public Value visit(DerefExp e, Env env, Heap h) { // New for reflang.
		Exp loc_exp = e.loc_exp();
		Value.RefVal loc = (Value.RefVal) loc_exp.accept(this, env, h);
		return h.deref(loc);
	}

	@Override
	public Value visit(AssignExp e, Env env, Heap h) { // New for reflang.
		Exp rhs = e.rhs_exp();
		Exp lhs = e.lhs_exp();
		//Note the order of evaluation below.
		Value rhs_val = (Value) rhs.accept(this, env, h);
		Value.RefVal loc = (Value.RefVal) lhs.accept(this, env, h);
		Value assign_val = h.setref(loc, rhs_val);
		return assign_val;
	}

	@Override
	public Value visit(FreeExp e, Env env, Heap h) { // New for reflang.
		Exp value_exp = e.value_exp();
		Value.RefVal loc = (Value.RefVal) value_exp.accept(this, env, h);
		h.free(loc);
		return new Value.UnitVal();
	}

	private Env initialEnv() {
		Env initEnv = new EmptyEnv();

		/* Procedure: (read <filename>). Following is same as (define read (lambda (file) (read file))) */
		List<String> formals = new ArrayList<>();
		formals.add("file");
		Exp body = new AST.ReadExp(new VarExp("file"));
		Value.FunVal readFun = new Value.FunVal(initEnv, formals, body);
		initEnv = new Env.ExtendEnv(initEnv, "read", readFun);

		/* Procedure: (require <filename>). Following is same as (define require (lambda (file) (eval (read file)))) */
		formals = new ArrayList<>();
		formals.add("file");
		body = new EvalExp(new AST.ReadExp(new VarExp("file")));
		Value.FunVal requireFun = new Value.FunVal(initEnv, formals, body);
		initEnv = new Env.ExtendEnv(initEnv, "require", requireFun);

		/* Add new built-in procedures here */ 

		return initEnv;
	}
	
	Reader _reader; 
	public Evaluator(Reader reader) {
		_reader = reader;
	}


	static class EvalThread extends Thread {
		Env env;
		Exp exp;
		Evaluator evaluator;
		Heap h;
		private volatile Value value;

		protected EvalThread(Env env, Exp exp, Evaluator evaluator, Heap h){
			this.env = env;
			this.exp = exp;
			this.evaluator = evaluator;
			this.h = h;
		}
		
		public void run(){
			value = (Value) exp.accept(evaluator, env, h);
		}
		
		public Value value(){
			try {
				this.join();
			} catch (InterruptedException e) {
				return new Value.DynamicError(e.getMessage());
			}
			return value;
		}
	}
	

	@Override
	public Value visit(ForkExp e, Env env, Heap h) {
        Exp fst = e.fst_exp();
        Exp snd = e.snd_exp();
        EvalThread fst_thread = new EvalThread(env, fst, this, h);
        EvalThread snd_thread = new EvalThread(env, snd, this, h);
        fst_thread.start();
        snd_thread.start();
        Value fst_val = fst_thread.value();
        Value snd_val = snd_thread.value();
		return new Value.PairVal(fst_val, snd_val);	
	}

	@Override
	public Value visit(LockExp e, Env env, Heap h) {
        Exp value_exp = e.value_exp();
        Object result = value_exp.accept(this, env, h);
		if(!(result instanceof Value.RefVal))
			return new Value.DynamicError("Non-reference values cannot be locked in expression " +  ts.visit(e, env, h));
        Value.RefVal loc = (Value.RefVal) result;
        loc.lock();
        return loc;
	}

	
	@Override
	public Value visit(UnlockExp e, Env env, Heap h) {
        Exp value_exp = e.value_exp();
        Object result = value_exp.accept(this, env, h);
		if(!(result instanceof Value.RefVal))
			return new Value.DynamicError("Non-reference values cannot be unlocked  in expression " +  ts.visit(e, env, h));
        Value.RefVal loc = (Value.RefVal) result;
        try{
        	loc.unlock();
        } catch(IllegalMonitorStateException ex){
        	return new Value.DynamicError("Lock held by another thread " +  ts.visit(e, env, h));
        }
		return loc;
	}

	@Override
	public Value visit(ProcExp e, Env env, Heap h) {
		return new Value.ActorVal(env, e.formals(), e.body(), this, h);
	}

	@Override
	public Value visit(SendExp e, Env env, Heap h) {
		Object result = e.operator().accept(this, env, h);
		if(!(result instanceof Value.ActorVal))
			return new Value.DynamicError("Operator not an actor in send " +  ts.visit(e, env, h));
		Value.ActorVal actor =  (Value.ActorVal) result; //Dynamic checking
		List<Exp> operands = e.operands();

		// Call-by-value semantics
		List<Value> actuals = new ArrayList<Value>(operands.size());
		for(Exp exp : operands) 
			actuals.add((Value)exp.accept(this, env, h));

		List<String> formals = actor.formals();
		if (formals.size()!=actuals.size())
			return new Value.DynamicError("Argument mismatch in send " + ts.visit(e, env, h));

		try {
			if(actor.receive(actuals)) {
				return new Value.UnitVal();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return new Value.DynamicError("Message send to dead actor in " + ts.visit(e, env, h));
	}

	@Override
	public Value visit(SelfExp e, Env env, Heap h) {
		Value result = env.get("self");
		if(!(result instanceof Value.ActorVal))
			return new Value.DynamicError("Self is not an actor in " +  ts.visit(e, env, h));
		return result;
	}

	@Override
	public Value visit(StopExp e, Env env, Heap h) {
		Value result = env.get("self");
		if(!(result instanceof Value.ActorVal))
			return new Value.DynamicError("Self is not an actor in " +  ts.visit(e, env, h));
		Value.ActorVal actor =  (Value.ActorVal) result; //Dynamic checking
		actor.exit();
		return new Value.UnitVal();
	}

}
