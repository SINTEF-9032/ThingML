/* ******** Start of template RcdPortForwardFlowContr.h */
//
// Place prototypes for /*PORT_NAME*/ here
void /*PORT_NAME*/_set_listener_id(uint16_t id);

#define /*PORT_NAME*/_TX_BUF_SIZE /*TX_BUF_SIZE*/
#define /*PORT_NAME*/_MTU_SIZE /*MTU_SIZE*/
#define /*PORT_NAME*/_SEQ_MOD 4
#define /*PORT_NAME*/_TIMEOUT_MS /*TIMEOUT_MS*/

bool /*PORT_NAME*/_receive_msg_or_ack( uint8_t *rcv_buf_ptr, uint8_t rcv_buf_len);
void /*PORT_NAME*/_timeout_check(void);
uint8_t /*PORT_NAME*/_remove_oldest_from_tx_buf(void);
void /*PORT_NAME*/_make_room_in_tx_buf( uint8_t req_len);
void /*PORT_NAME*/_put_into_tx_buf( uint8_t *buf_ptr, uint8_t buf_len);
void /*PORT_NAME*/_send_oldest_in_tx_buf(void);

struct /*PORT_NAME*/_instance_type {
    uint16_t listener_id;
    /*INSTANCE_INFORMATION*/
    uint16_t tx_buf_rd_idx;
    uint16_t tx_buf_wr_idx;
    uint8_t rx_seq_num;
    uint8_t tx_seq_num;
    uint8_t tx_buf[/*PORT_NAME*/_TX_BUF_SIZE];
    uint32_t last_sent_tick;
} /*PORT_NAME*/_instance;

/* ******** End of template RcdPortForwardFlowContr.h */
