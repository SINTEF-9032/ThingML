/* ******** Start of template RcdPortShimAck.h */
//
// This file will be parsed and content between xxx_START and xxx_END will be extracted

/*SHIM_PROTO_START*/

// Place prototypes for /*PORT_NAME*/ here

#define /*PORT_NAME*/_TX_BUF_SIZE /*TX_BUF_SIZE*/
#define /*PORT_NAME*/_MTU_SIZE /*MTU_SIZE*/
#define /*PORT_NAME*/_SEQ_MOD 4
#define /*PORT_NAME*/_TIMEOUT_MS /*TIMEOUT_MS*/

void to_shim_/*PORT_NAME*/( uint8_t *rcv_buf_ptr, uint8_t rcv_buf_len);
void from_transport_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len);

void /*PORT_NAME*/_timeout_check(void);
uint8_t /*PORT_NAME*/_remove_oldest_from_tx_buf(void);
void /*PORT_NAME*/_make_room_in_tx_buf( uint8_t req_len);
void /*PORT_NAME*/_send_oldest_in_tx_buf(void);

/*SHIM_PROTO_END*/

/*SHIM_INFO_START*/
// Place instance vars for /*PORT_NAME*/ here

uint16_t tx_buf_rd_idx;
uint16_t tx_buf_wr_idx;
uint8_t rx_seq_num;
uint8_t tx_seq_num;
uint8_t tx_buf[/*PORT_NAME*/_TX_BUF_SIZE];
uint32_t last_sent_tick;

/*SHIM_INFO_END*/

/* ******** End of template RcdPortShimAck.h */
