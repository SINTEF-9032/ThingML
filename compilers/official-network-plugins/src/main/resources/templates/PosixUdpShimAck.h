/* ******** Start of template PosixUdpShimAck.h */
//
// This file will be parsed and content between xxx_START and xxx_END will be extracted

/*SHIM_PROTO_START*/

// Place prototypes for /*PORT_NAME*/ here

#define /*PORT_NAME*/_TX_BUF_SIZE /*TX_BUF_SIZE*/
#define /*PORT_NAME*/_MTU_SIZE /*MTU_SIZE*/
#define /*PORT_NAME*/_SEQ_MOD 4
#define /*PORT_NAME*/_TIMEOUT_MS /*TIMEOUT_MS*/

static unsigned long long get_system_time_/*PORT_NAME*/(void);
static void to_shim_/*PORT_NAME*/( unsigned char *rcv_buf_ptr, unsigned int rcv_buf_len);
static void from_transport_/*PORT_NAME*/( unsigned char *buf_ptr, unsigned int buf_len);

static void /*PORT_NAME*/_timeout_check(void);
static unsigned int /*PORT_NAME*/_remove_oldest_from_tx_buf(void);
static void /*PORT_NAME*/_make_room_in_tx_buf( unsigned int req_len);
static void /*PORT_NAME*/_send_oldest_in_tx_buf(void);

/*SHIM_PROTO_END*/

/*SHIM_INFO_START*/
// Place instance vars for /*PORT_NAME*/ here

unsigned int tx_buf_rd_idx;
unsigned int tx_buf_wr_idx;
unsigned int rx_seq_num;
unsigned int tx_seq_num;
unsigned char tx_buf[/*PORT_NAME*/_TX_BUF_SIZE];
unsigned long long last_sent_ms;

/*SHIM_INFO_END*/

/* ******** End of template PosixUdpShimAck.h */
