package msglang;

import java.util.List;

import msglang.AST.*;

public class Printer {
	public void print(Value v) {
		if(v.tostring() != "")
			System.out.println(v.tostring());
	}
	public void print(Exception e) {
		System.out.println(e.toString());
	}
	
	public static class Formatter implements AST.Visitor<String> {
		
		public String visit(AST.AddExp e, Env env, Heap h) {
			String result = "(+ ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}
		
		public String visit(AST.UnitExp e, Env env, Heap h) {
			return "unit";
		}

		public String visit(AST.NumExp e, Env env, Heap h) {
			return "" + e.v();
		}
		
		public String visit(AST.StrExp e, Env env, Heap h) {
			return e.v();
		}
		
		public String visit(AST.BoolExp e, Env env, Heap h) {
			if(e.v()) return "#t";
			return "#f";
		}

		public String visit(AST.DivExp e, Env env, Heap h) {
			String result = "(/ ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}
		
		public String visit(AST.ErrorExp e, Env env, Heap h) {
			return e.toString();
		}
		
		public String visit(AST.ReadExp e, Env env, Heap h) {
			return "(read " + e.file().accept(this, env, h) + ")";
		}

		public String visit(AST.EvalExp e, Env env, Heap h) {
			return "(eval " + e.code().accept(this, env, h) + ")";
		}

		public String visit(AST.MultExp e, Env env, Heap h) {
			String result = "(* ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}
		
		public String visit(AST.Program p, Env env, Heap h) {
			return "" + p.e().accept(this, env, h);
		}
		
		public String visit(AST.SubExp e, Env env, Heap h) {
			String result = "(- ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}
		
		public String visit(AST.VarExp e, Env env, Heap h) {
			return "" + e.name();
		}
		
		public String visit(AST.LetExp e, Env env, Heap h) {
			String result = "(let (";
			List<String> names = e.names();
			List<Exp> value_exps = e.value_exps();
			int num_decls = names.size();
			for (int i = 0; i < num_decls ; i++) {
				result += " (";
				result += names.get(i) + " ";
				result += value_exps.get(i).accept(this, env, h) + ")";
			}
			result += ") ";
			result += e.body().accept(this, env, h) + " ";
			return result + ")";
		}
		
		public String visit(AST.DefineDecl d, Env env, Heap h) {
			String result = "(define ";
			result += d.name() + " ";
			result += d.value_exp().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.LambdaExp e, Env env, Heap h) {
			String result = "(lambda ( ";
			for(String formal : e.formals()) 
				result += formal + " ";
			result += ") ";
			result += e.body().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.CallExp e, Env env, Heap h) {
			String result = "(";
			result += e.operator().accept(this, env, h) + " ";
			for(AST.Exp exp : e.operands())
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}
		
		public String visit(AST.IfExp e, Env env, Heap h) {
			String result = "(if ";
			result += e.conditional().accept(this, env, h) + " ";
			result += e.then_exp().accept(this, env, h) + " ";
			result += e.else_exp().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.LessExp e, Env env, Heap h) {
			String result = "(< ";
			result += e.first_exp().accept(this, env, h) + " ";
			result += e.second_exp().accept(this, env, h);
			return result + ")";
		}

		public String visit(AST.EqualExp e, Env env, Heap h) {
			String result = "(= ";
			result += e.first_exp().accept(this, env, h) + " ";
			result += e.second_exp().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.GreaterExp e, Env env, Heap h) {
			String result = "(> ";
			result += e.first_exp().accept(this, env, h) + " ";
			result += e.second_exp().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.CarExp e, Env env, Heap h) {
			String result = "(car ";
			result += e.arg().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.CdrExp e, Env env, Heap h) {
			String result = "(cdr ";
			result += e.arg().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.ConsExp e, Env env, Heap h) {
			String result = "(cons ";
			result += e.fst().accept(this, env, h) + " ";
			result += e.snd().accept(this, env, h);
			return result + ")";
		}
		
		public String visit(AST.ListExp e, Env env, Heap h) {
			String result = "(list ";
			for(AST.Exp exp : e.elems())
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}

		public String visit(AST.NullExp e, Env env, Heap h) {
			String result = "(null? ";
			result += e.arg().accept(this, env, h);
			return result + ")";
		}
		
        @Override
        public String visit(IsListExp e, Env env, Heap h) {
                String result = "(list? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsPairExp e, Env env, Heap h) {
                String result = "(pair? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsUnitExp e, Env env, Heap h) {
                String result = "(unit? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsProcedureExp e, Env env, Heap h) {
                String result = "(procedure? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsStringExp e, Env env, Heap h) {
                String result = "(string? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsNumberExp e, Env env, Heap h) {
                String result = "(number? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsBooleanExp e, Env env, Heap h) {
                String result = "(boolean? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        @Override
        public String visit(IsNullExp e, Env env, Heap h) {
                String result = "(null? ";
                result += e.exp().accept(this, env, h);
                return result + ")";
        }

        public String visit(AST.LetrecExp e, Env env, Heap h) {
			String result = "(letrec (";
			List<String> names = e.names();
			List<Exp> fun_exps = e.fun_exps();
			int num_decls = names.size();
			for (int i = 0; i < num_decls ; i++) {
				result += " (";
				result += names.get(i) + " ";
				result += fun_exps.get(i).accept(this, env, h) + ")";
			}
			result += ") ";
			result += e.body().accept(this, env, h) + " ";
			return result + ")";
		}

        public String visit(AST.RefExp e, Env env, Heap h) {
                String result = "(ref ";
                result += e.value_exp().accept(this, env, h);
                return result + ")";
        }

        public String visit(AST.DerefExp e, Env env, Heap h) {
                String result = "(deref ";
                result += e.loc_exp().accept(this, env, h);
                return result + ")";
        }

        public String visit(AST.AssignExp e, Env env, Heap h) {
                String result = "(set! ";
                result += e.lhs_exp().accept(this, env, h) + " ";
                result += e.rhs_exp().accept(this, env, h);
                return result + ")";
        }
        
        public String visit(AST.FreeExp e, Env env, Heap h) {
            String result = "(free ";
            result += e.value_exp().accept(this, env, h);
            return result + ")";
        }

		@Override
		public String visit(ForkExp e, Env env, Heap h) {
            String result = "(fork ";
            result += e.fst_exp().accept(this, env, h) + " ";
            result += e.snd_exp().accept(this, env, h);
            return result + ")";
		}

		@Override
		public String visit(LockExp e, Env env, Heap h) {
            String result = "(lock ";
            result += e.value_exp().accept(this, env, h);
            return result + ")";
		}

		@Override
		public String visit(UnlockExp e, Env env, Heap h) {
            String result = "(unlock ";
            result += e.value_exp().accept(this, env, h);
            return result + ")";
		}

		@Override
		public String visit(ProcExp e, Env env, Heap h) {
			String result = "(actor ( ";
			for(String formal : e.formals()) 
				result += formal + " ";
			result += ") ";
			result += e.body().accept(this, env, h);
			return result + ")";
		}

		@Override
		public String visit(SendExp e, Env env, Heap h) {
			String result = "(send ";
			result += e.operator().accept(this, env, h) + " ";
			for(AST.Exp exp : e.operands())
				result += exp.accept(this, env, h) + " ";
			return result + ")";
		}

		@Override
		public String visit(SelfExp e, Env env, Heap h) {
			return "(self)";
		}

		@Override
		public String visit(StopExp e, Env env, Heap h) {
			return "(stop)";
		}

	}
}
