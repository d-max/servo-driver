#include <msp430g2553.h>
#include "servo.h"

void _configure_servos() {
	/* config servos */
	servos[0].port = PORT1;
	servos[0].bit = BIT6;
	servos[0].pwm_time = PWM_MAX_TIME;

	servos[1].port = PORT1;
	servos[1].bit = BIT7;
	servos[1].pwm_time = PWM_MAX_TIME;

	// config pins
	P1OUT = 0;
	P1DIR = servos[0].bit + servos[1].bit;

	/* config timer A */
	// set time of pdm one iteration - 1/50 of second
	TACCR0 = PWM_PERIOD_TIME;
	// enable interruption caused by CCR0 and CCR1 values
	TACCTL0 = CCIE;
	TACCTL1 = CCIE;
	// sub-main clock + 4-divider + up mode + initialize
	TACTL = TASSEL_2 + ID_2 + MC_1 + TACLR;
	// enable interruptions
	_BIS_SR(GIE);
}

int srv_index = 0;
struct servo * nextServo() {
	if (++srv_index >= SRV_COUNT) srv_index = 0;
	return currentServo();
}

struct servo * currentServo() {
	return &servos[srv_index];
}

#pragma vector = TIMER0_A0_VECTOR
__interrupt void CCR0_ISR(void) {
    struct servo *s = nextServo();
    // set pwm duration time
    TACCR1 = (*s).pwm_time;
    // set out pin bit to 1 
    switch ((*s).port) {
		case PORT1:
			P1OUT |= (*s).bit;
			break;
		case PORT2:
			P2OUT |= (*s).bit;
			break;
    }
    // reset interruption flag
    TA0CCTL0 &= ~CCIFG;
}

#pragma vector = TIMER0_A1_VECTOR
__interrupt void CCR1_ISR(void) {
    struct servo *s = currentServo();
    // set out pin bit to 0 
    switch ((*s).port) {
		case PORT1:
			P1OUT &= ~(*s).bit;
			break;
		case PORT2:
			P2OUT &= ~(*s).bit;
			break;
    }
    // reset interruption flag
    TA0CCTL1 &= ~CCIFG;
}