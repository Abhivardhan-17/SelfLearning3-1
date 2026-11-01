
import { useEffect, useState } from 'react'
import {
  Search,
  Send,
  MessageCircle,
  ArrowLeft,
  MoreVertical
} from 'lucide-react'
import api from './api'

export default function Messenger() {
  const [query, setQuery] = useState('')
  const [users, setUsers] = useState([])
  const [recentChats, setRecentChats] = useState([])
  const [selectedUser, setSelectedUser] = useState(null)
  const [messages, setMessages] = useState([])
  const [content, setContent] = useState('')
  const [loadingUsers, setLoadingUsers] = useState(false)
  const [loadingMessages, setLoadingMessages] = useState(false)
  const [loadingRecent, setLoadingRecent] = useState(true)
  const [error, setError] = useState('')
  const [openMenu, setOpenMenu] = useState(null)

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem('user') || '{}')
    } catch {
      return {}
    }
  })()

  const loadRecentChats = async () => {
    try {
      const { data } = await api.get('/messages/recent')
      setRecentChats(data)
    } catch {
      setError('Could not load recent conversations.')
    } finally {
      setLoadingRecent(false)
    }
  }

  useEffect(() => {
    loadRecentChats()

    const timer = setInterval(loadRecentChats, 3000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    const searchUsers = async () => {
      if (!query.trim()) {
        setUsers([])
        return
      }

      setLoadingUsers(true)

      try {
        const { data } = await api.get(
          `/users/search?q=${encodeURIComponent(query.trim())}`
        )
        setUsers(data)
        setError('')
      } catch {
        setError('Could not search users.')
      } finally {
        setLoadingUsers(false)
      }
    }

    const timer = setTimeout(searchUsers, 350)
    return () => clearTimeout(timer)
  }, [query])

  const loadMessages = async userId => {
    try {
      const { data } = await api.get(
        `/messages/conversation/${userId}`
      )
      setMessages(data)
      setError('')
      await loadRecentChats()
    } catch {
      setError('Could not load this conversation.')
    }
  }

  useEffect(() => {
    if (!selectedUser) return

    let active = true

    const fetchMessages = async () => {
      try {
        const { data } = await api.get(
          `/messages/conversation/${selectedUser.id}`
        )

        if (active) {
          setMessages(data)
          setError('')
          loadRecentChats()
        }
      } catch {
        if (active) {
          setError('Could not load this conversation.')
        }
      } finally {
        if (active) {
          setLoadingMessages(false)
        }
      }
    }

    setLoadingMessages(true)
    setMessages([])
    setOpenMenu(null)
    fetchMessages()

    const timer = setInterval(fetchMessages, 3000)

    return () => {
      active = false
      clearInterval(timer)
    }
  }, [selectedUser])

  const openConversation = user => {
    setSelectedUser({
      id: user.id ?? user.userId,
      username: user.username
    })
    setMessages([])
    setError('')
  }

  const sendMessage = async e => {
    e.preventDefault()

    if (!content.trim() || !selectedUser) return

    const messageText = content.trim()
    setContent('')
    setError('')

    try {
      await api.post('/messages', {
        receiverId: selectedUser.id,
        content: messageText
      })

      await loadMessages(selectedUser.id)
    } catch {
      setContent(messageText)
      setError('Message could not be sent. Please try again.')
    }
  }

  const unsendMessage = async messageId => {
    const confirmed = window.confirm(
      'Are you sure you want to unsend this message?'
    )

    if (!confirmed) return

    try {
      setError('')

      await api.delete(`/messages/${messageId}`)

      setMessages(previous =>
        previous.filter(message => message.id !== messageId)
      )

      setOpenMenu(null)

      await loadRecentChats()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Could not unsend this message. Please try again.'
      )
    }
  }

  const formatTime = date => {
    if (!date) return ''

    const messageDate = new Date(date)

    if (Number.isNaN(messageDate.getTime())) return ''

    const today = new Date()

    if (messageDate.toDateString() === today.toDateString()) {
      return messageDate.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    return messageDate.toLocaleDateString([], {
      day: '2-digit',
      month: 'short'
    })
  }

  return (
    <main className="messenger-page">
      <header className="messenger-heading">
        <div>
          <h2><MessageCircle size={25} /> Messenger</h2>
          <p className="muted">
            Connect and chat with your community.
          </p>
        </div>
      </header>

      <div className="messenger-layout">
        <section
          className={`messenger-users ${
            selectedUser ? 'mobile-hidden' : ''
          }`}
        >
          <h3>Messages</h3>

          {!query.trim() && (
            <>
              <h4 className="messenger-section-label">
                Recent conversations
              </h4>

              {loadingRecent ? (
                <p className="muted">Loading conversations...</p>
              ) : recentChats.length === 0 ? (
                <div className="messenger-hint">
                  No conversations yet. Search for someone to start chatting.
                </div>
              ) : (
                <div className="messenger-user-list">
                  {recentChats.map(chat => (
                    <button
                      key={chat.userId}
                      className={`messenger-user ${
                        selectedUser?.id === chat.userId ? 'active' : ''
                      }`}
                      onClick={() => openConversation(chat)}
                    >
                      <span className="avatar">
                        {chat.username?.[0]?.toUpperCase()}
                      </span>

                      <span className="messenger-user-info">
                        <b>{chat.username}</b>

                        <small className="messenger-last-message">
                          {chat.lastMessageSenderId === currentUser.id
                            ? 'You: '
                            : ''}
                          {chat.lastMessage}
                        </small>
                      </span>

                      <span className="messenger-conversation-meta">
                        <small>
                          {formatTime(chat.lastMessageAt)}
                        </small>

                        {chat.unreadCount > 0 && (
                          <span className="messenger-unread-badge">
                            {chat.unreadCount}
                          </span>
                        )}
                      </span>
                    </button>
                  ))}
                </div>
              )}
            </>
          )}

          <h4 className="messenger-section-label">
            Find people
          </h4>

          <div className="messenger-search">
            <Search size={19} />
            <input
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Search people..."
            />
          </div>

          {loadingUsers && (
            <p className="muted">Searching...</p>
          )}

          {query.trim() && !loadingUsers && users.length === 0 && (
            <div className="messenger-hint">
              No users found.
            </div>
          )}

          <div className="messenger-user-list">
            {users.map(user => (
              <button
                key={user.id}
                className={`messenger-user ${
                  selectedUser?.id === user.id ? 'active' : ''
                }`}
                onClick={() => openConversation(user)}
              >
                <span className="avatar">
                  {user.username?.[0]?.toUpperCase()}
                </span>

                <span className="messenger-user-info">
                  <b>{user.username}</b>
                  <small>Start a conversation</small>
                </span>
              </button>
            ))}
          </div>
        </section>

        <section
          className={`messenger-chat ${
            !selectedUser ? 'mobile-hidden' : ''
          }`}
        >
          {selectedUser ? (
            <>
              <div className="messenger-chat-header">
                <button
                  className="icon-btn messenger-back"
                  onClick={() => setSelectedUser(null)}
                  title="Back to messages"
                >
                  <ArrowLeft size={20} />
                </button>

                <div className="avatar">
                  {selectedUser.username?.[0]?.toUpperCase()}
                </div>

                <div>
                  <b>{selectedUser.username}</b>
                  <small>KLMedia member</small>
                </div>
              </div>

              <div className="messenger-messages">
                {loadingMessages ? (
                  <p className="muted center">
                    Loading messages...
                  </p>
                ) : messages.length === 0 ? (
                  <div className="messenger-empty">
                    <MessageCircle size={35} />
                    <h3>Start a conversation</h3>
                    <p>
                      Send your first message to {selectedUser.username}.
                    </p>
                  </div>
                ) : (
                  messages.map(message => (
                    <div
                      key={message.id}
                      className={`message-row ${
                        message.senderId === currentUser.id
                          ? 'sent'
                          : 'received'
                      }`}
                    >
                      <div className="message-bubble">
                        <p>{message.content}</p>

                        <small>
                          {message.sentAt
                            ? new Date(message.sentAt).toLocaleTimeString(
                                [],
                                {
                                  hour: '2-digit',
                                  minute: '2-digit'
                                }
                              )
                            : ''}
                        </small>
                      </div>

                      {message.senderId === currentUser.id && (
                        <div className="message-options">
                          <button
                            className="message-menu-button"
                            onClick={() =>
                              setOpenMenu(
                                openMenu === message.id
                                  ? null
                                  : message.id
                              )
                            }
                            title="Message options"
                          >
                            <MoreVertical size={18} />
                          </button>

                          {openMenu === message.id && (
                            <div className="message-menu">
                              <button
                                className="unsend-button"
                                onClick={() =>
                                  unsendMessage(message.id)
                                }
                              >
                                Unsend
                              </button>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>

              <form
                className="messenger-compose"
                onSubmit={sendMessage}
              >
                <input
                  value={content}
                  onChange={e => setContent(e.target.value)}
                  placeholder="Write a message..."
                  maxLength={2000}
                  required
                />

                <button
                  className="primary"
                  type="submit"
                  disabled={!content.trim()}
                  title="Send message"
                >
                  <Send size={19} />
                </button>
              </form>
            </>
          ) : (
            <div className="messenger-welcome">
              <MessageCircle size={55} />
              <h2>Your messages</h2>
              <p>
                Select a recent conversation or search for someone to chat.
              </p>
            </div>
          )}
        </section>
      </div>

      {error && (
        <p className="error messenger-error">{error}</p>
      )}
    </main>
  )
}
