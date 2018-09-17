package com.aminography.primeadapter.exception

/**
 * Created by aminography on 9/7/2018.
 */
class ViewHolderNotFoundException(
        dataHolderClass: Class<*>?,
        adapterClass: Class<*>?
) : RuntimeException("A suitable ViewHolder not found for [${dataHolderClass?.canonicalName}].\n" +
        "Do you have returned right value for it?\n" +
        "Please check the makeViewHolder method in [${adapterClass?.canonicalName}]."
)