/* ******** Start of template RcdPortShimNone.c */

void /*CFG_CPPNAME_SCOPE*/to_shim_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len){
    to_transport_/*PORT_NAME*/( buf_ptr, buf_len);
}

void /*CFG_CPPNAME_SCOPE*/from_transport_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len) {
    from_shim_/*PORT_NAME*/( buf_ptr, buf_len);
}

/* ******** End of template RcdPortShimNone.c */
