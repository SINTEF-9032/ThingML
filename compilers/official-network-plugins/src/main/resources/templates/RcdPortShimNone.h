/* ******** Start of template RcdPortShimNone.h */
//
// This file will be parsed and content between xxx_START and xxx_END will be extracted

/*SHIM_PROTO_START*/

// Place prototypes for /*PORT_NAME*/ here

void to_shim_/*PORT_NAME*/( uint8_t *rcv_buf_ptr, uint8_t rcv_buf_len);
void from_transport_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len);

/*SHIM_PROTO_END*/

/*SHIM_INFO_START*/
// Place instance vars for /*PORT_NAME*/ here

/*SHIM_INFO_END*/

/* ******** End of template RcdPortShimNone.h */
