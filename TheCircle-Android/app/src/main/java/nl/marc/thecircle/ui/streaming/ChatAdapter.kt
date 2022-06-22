package nl.marc.thecircle.ui.streaming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nl.marc.thecircle.databinding.ListItemReceivedMessageBinding
import nl.marc.thecircle.databinding.ListItemSentMessageBinding
import nl.marc.thecircle.domain.Message
import nl.marc.thecircle.domain.User

class ChatAdapter(
    private val currentUserId: String
) : ListAdapter<Pair<Message, User?>, ChatAdapter.MessageViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> MessageViewHolder.SentMessageViewHolder(
                ListItemSentMessageBinding.inflate(inflater, parent, false)
            )
            else -> MessageViewHolder.ReceivedMessageViewHolder(
                ListItemReceivedMessageBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val (message, user) = currentList.elementAtOrNull(position) ?: return

        if (holder is MessageViewHolder.SentMessageViewHolder) {
            holder.binding.labelMessage.text = message.message
        } else if (holder is MessageViewHolder.ReceivedMessageViewHolder) {
            holder.binding.labelMessage.text = message.message
            holder.binding.labelUsername.text = user?.name ?: message.senderId
        }
    }

    override fun getItemViewType(position: Int): Int {
        val (message, _) = currentList.elementAt(position)
        return if (message.senderId == currentUserId) {
            TYPE_SENT
        } else {
            TYPE_RECEIVED
        }
    }

    sealed class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class SentMessageViewHolder(val binding: ListItemSentMessageBinding) : MessageViewHolder(binding.root)
        class ReceivedMessageViewHolder(val binding: ListItemReceivedMessageBinding) : MessageViewHolder(binding.root)
    }

    object DiffCallback : DiffUtil.ItemCallback<Pair<Message, User?>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Message, User?>,
            newItem: Pair<Message, User?>
        ): Boolean {
            return oldItem.first.messageId == newItem.first.messageId
        }

        override fun areContentsTheSame(
            oldItem: Pair<Message, User?>,
            newItem: Pair<Message, User?>
        ): Boolean {
            return oldItem.first == newItem.first && oldItem.second == newItem.second
        }
    }

    companion object {
        const val TYPE_SENT = 0
        const val TYPE_RECEIVED = 1
    }
}
