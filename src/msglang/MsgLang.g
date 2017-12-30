grammar MsgLang;
import ForkLang; 
 
exp returns [Exp ast]: 
		va=varexp { $ast = $va.ast; }
		| num=numexp { $ast = $num.ast; }
		| str=strexp { $ast = $str.ast; }
		| bl=boolexp { $ast = $bl.ast; }
        | add=addexp { $ast = $add.ast; }
        | sub=subexp { $ast = $sub.ast; }
        | mul=multexp { $ast = $mul.ast; }
        | div=divexp { $ast = $div.ast; }
        | let=letexp { $ast = $let.ast; }
        | lam=lambdaexp { $ast = $lam.ast; }
        | call=callexp { $ast = $call.ast; }
        | i=ifexp { $ast = $i.ast; }
        | less=lessexp { $ast = $less.ast; }
        | eq=equalexp { $ast = $eq.ast; }
        | gt=greaterexp { $ast = $gt.ast; }
        | car=carexp { $ast = $car.ast; }
        | cdr=cdrexp { $ast = $cdr.ast; }
        | cons=consexp { $ast = $cons.ast; }
        | list=listexp { $ast = $list.ast; }
        | nl=nullexp { $ast = $nl.ast; }
        | lrec=letrecexp { $ast = $lrec.ast; }
        | ref=refexp { $ast = $ref.ast; }
        | deref=derefexp { $ast = $deref.ast; }
        | assign=assignexp { $ast = $assign.ast; }
        | free=freeexp { $ast = $free.ast; }
        | fork=forkexp { $ast = $fork.ast; }
        | lock=lockexp { $ast = $lock.ast; }
        | ulock=unlockexp { $ast = $ulock.ast; }
        | proc=procexp { $ast = $proc.ast; }
        | send=sendexp { $ast = $send.ast; }
        | stp=stopexp { $ast = $stp.ast; }
        | self=selfexp { $ast = $self.ast; }
        ;
 
 procexp returns [ProcExp ast] 
        locals [ArrayList<String> formals = new ArrayList<String>(); ] : 
 		'(' Process  
 			'(' ( id=Identifier { $formals.add($id.text); } )* ')' 
 			body=exp  
 		')'  { $ast = new ProcExp($formals, $body.ast); }
 		;

 sendexp returns [SendExp ast] 
 		locals [ArrayList<Exp> actuals = new ArrayList<Exp>(); ] : 
 		'(' Send 
 			receiver=exp 
 			( argument=exp { $actuals.add($argument.ast); } )*  
 		')' { $ast = new SendExp($receiver.ast, $actuals); }
 		;

 stopexp returns [StopExp ast] : 
 		'(' Stop 
 		')' { $ast = new StopExp(); }
 		;

 selfexp returns [SelfExp ast] : 
 		'(' Self 
 		')' { $ast = new SelfExp(); }
 		;
