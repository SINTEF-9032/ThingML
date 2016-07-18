/* ******** Start of template RcdPortTransportNone.h */
//
// This file will be parsed and content between xxx_START and xxx_END will be extracted

/*TRANSPORT_PROTO_START*/

// Place prototypes for /*PORT_NAME*/ here

void to_transport_/*PORT_NAME*/( uint8_t *rcv_buf_ptr, uint8_t rcv_buf_len);
void from_link_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len);

/*TRANSPORT_PROTO_END*/

/*TRANSPORT_INFO_START*/
// Place instance vars for /*PORT_NAME*/ here

/*TRANSPORT_INFO_END*/

/* ******** End of template RcdPortTransportNone.h */
