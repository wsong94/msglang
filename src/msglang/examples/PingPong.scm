(define ping 
	(process (sender num)
		(if
			(> num 0) (send sender (self) (- num 1))  
			(stop)
		)
	)
)

(define pong 
	(process (sender num)
		(if
			(> num 0) (send sender (self) (- num 1))  
			(stop)
		)
	)
) 

(send ping pong 342)